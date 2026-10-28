package com.residencial.finance.infrastructure.web;

import com.residencial.finance.application.dto.FixedAssetDto;
import com.residencial.finance.application.dto.RegisterAssetRequest;
import com.residencial.finance.application.dto.RegisterAssetResponse;
import com.residencial.finance.application.dto.UpdateAssetStatusRequest;
import com.residencial.finance.application.usecase.ListFixedAssetsUseCase;
import com.residencial.finance.application.usecase.RegisterFixedAssetUseCase;
import com.residencial.finance.application.usecase.UpdateAssetStatusUseCase;
import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finance/assets")
public class FixedAssetController {

    private final RegisterFixedAssetUseCase registerFixedAssetUseCase;
    private final UpdateAssetStatusUseCase updateAssetStatusUseCase;
    private final ListFixedAssetsUseCase listFixedAssetsUseCase;

    public FixedAssetController(RegisterFixedAssetUseCase registerFixedAssetUseCase,
                                UpdateAssetStatusUseCase updateAssetStatusUseCase,
                                ListFixedAssetsUseCase listFixedAssetsUseCase) {
        this.registerFixedAssetUseCase = registerFixedAssetUseCase;
        this.updateAssetStatusUseCase = updateAssetStatusUseCase;
        this.listFixedAssetsUseCase = listFixedAssetsUseCase;
    }

    @PostMapping
    public ResponseEntity<RegisterAssetResponse> registerAsset(@Valid @RequestBody RegisterAssetRequest request) {
        RegisterAssetResponse response = registerFixedAssetUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FixedAssetDto> updateAssetStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAssetStatusRequest request) {
        FixedAssetDto response = updateAssetStatusUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FixedAssetDto>> listAssets(
            @RequestParam(required = false) AssetCategory category,
            @RequestParam(required = false) AssetStatus status) {
        List<FixedAssetDto> response = listFixedAssetsUseCase.execute(category, status);
        return ResponseEntity.ok(response);
    }
}
