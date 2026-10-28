package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.ParsedBankRecord;
import com.residencial.finance.application.dto.ReconciliationSummaryResponse;
import com.residencial.finance.domain.model.BankTransaction;
import com.residencial.finance.domain.model.BankTransactionStatus;
import com.residencial.finance.domain.model.MaintenanceCharge;
import com.residencial.finance.domain.repository.BankTransactionRepository;
import com.residencial.finance.domain.repository.MaintenanceChargeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReconcileBankPaymentsUseCase {

    private final MaintenanceChargeRepository maintenanceChargeRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public ReconcileBankPaymentsUseCase(
            MaintenanceChargeRepository maintenanceChargeRepository,
            BankTransactionRepository bankTransactionRepository) {
        this.maintenanceChargeRepository = maintenanceChargeRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    public ReconciliationSummaryResponse execute(MultipartFile file) {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "bank_report";
        List<ParsedBankRecord> records = parseFile(file);

        int totalRead = records.size();
        int reconciled = 0;
        int duplicates = 0;
        int unmatched = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<String> auditMessages = new ArrayList<>();

        for (ParsedBankRecord record : records) {
            String authNum = record.authorizationNumber();

            // 1. Check duplicate authorization number
            if (bankTransactionRepository.existsByAuthorizationNumber(authNum)) {
                duplicates++;
                auditMessages.add("Duplicate authorization: " + authNum + " was already processed.");
                continue;
            }

            // 2. Match with pending maintenance charge by house number reference
            Optional<MaintenanceCharge> chargeOpt = maintenanceChargeRepository.findPendingByHouseNumber(record.houseReference());

            if (chargeOpt.isPresent()) {
                MaintenanceCharge charge = chargeOpt.get();
                charge.applyPayment(record.amount());
                maintenanceChargeRepository.save(charge);

                BankTransaction tx = new BankTransaction(
                        authNum,
                        record.houseReference(),
                        charge,
                        record.transactionDate(),
                        record.amount(),
                        originalFilename,
                        BankTransactionStatus.RECONCILED,
                        null
                );
                bankTransactionRepository.save(tx);

                reconciled++;
                totalAmount = totalAmount.add(record.amount());
                auditMessages.add("Reconciled auth " + authNum + " for house " + record.houseReference() + " (" + record.amount() + ").");
            } else {
                // Save unmatched for manual auditor review without failing batch
                BankTransaction tx = new BankTransaction(
                        authNum,
                        record.houseReference(),
                        null,
                        record.transactionDate(),
                        record.amount(),
                        originalFilename,
                        BankTransactionStatus.UNMATCHED,
                        "No pending charge found for house reference: " + record.houseReference()
                );
                bankTransactionRepository.save(tx);

                unmatched++;
                auditMessages.add("Unmatched auth " + authNum + ": No pending charge for house " + record.houseReference() + ".");
            }
        }

        return new ReconciliationSummaryResponse(
                originalFilename,
                totalRead,
                reconciled,
                duplicates,
                unmatched,
                totalAmount,
                auditMessages
        );
    }

    private List<ParsedBankRecord> parseFile(MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (filename.endsWith(".csv")) {
            return parseCsv(file);
        } else {
            return parseExcel(file);
        }
    }

    private List<ParsedBankRecord> parseCsv(MultipartFile file) {
        List<ParsedBankRecord> records = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip header row
                }
                String[] tokens = line.split("[,;]");
                if (tokens.length >= 4) {
                    String auth = tokens[0].trim();
                    String ref = tokens[1].trim();
                    LocalDate date = LocalDate.parse(tokens[2].trim(), formatter);
                    BigDecimal amount = new BigDecimal(tokens[3].trim());
                    records.add(new ParsedBankRecord(auth, ref, date, amount));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse CSV bank statement: " + e.getMessage(), e);
        }
        return records;
    }

    private List<ParsedBankRecord> parseExcel(MultipartFile file) {
        List<ParsedBankRecord> records = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isHeader = true;

            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip header row
                }
                Cell cellAuth = row.getCell(0);
                Cell cellRef = row.getCell(1);
                Cell cellDate = row.getCell(2);
                Cell cellAmount = row.getCell(3);

                if (cellAuth == null || cellRef == null || cellAmount == null) continue;

                String auth = dataFormatter.formatCellValue(cellAuth).trim();
                String ref = dataFormatter.formatCellValue(cellRef).trim();
                if (auth.isEmpty() || ref.isEmpty()) continue;

                LocalDate date;
                if (cellDate != null && DateUtil.isCellDateFormatted(cellDate)) {
                    date = cellDate.getLocalDateTimeCellValue().toLocalDate();
                } else if (cellDate != null) {
                    date = LocalDate.parse(dataFormatter.formatCellValue(cellDate).trim(), formatter);
                } else {
                    date = LocalDate.now();
                }

                BigDecimal amount = new BigDecimal(dataFormatter.formatCellValue(cellAmount).trim().replace(",", ""));
                records.add(new ParsedBankRecord(auth, ref, date, amount));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Excel bank statement: " + e.getMessage(), e);
        }
        return records;
    }
}
