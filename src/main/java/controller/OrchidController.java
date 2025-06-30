package controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pojo.Orchid;
import service.OrchidService;

import java.util.List;

@RestController
@RequestMapping("/api/orchids")
@Tag(name = "Orchid Management", description = "APIs for managing orchids")
public class OrchidController {
    
    @Autowired
    private OrchidService orchidService;
    
    @Operation(summary = "Get all orchids", description = "Returns list of all orchids")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved orchids",
            content = @Content(schema = @Schema(implementation = Orchid.class)))
    @GetMapping
    public ResponseEntity<List<Orchid>> getAllOrchids() {
        List<Orchid> orchids = orchidService.getAllOrchids();
        return ResponseEntity.ok(orchids);
    }
    
    @Operation(summary = "Get orchid by ID", description = "Returns a specific orchid by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orchid found",
                    content = @Content(schema = @Schema(implementation = Orchid.class))),
            @ApiResponse(responseCode = "404", description = "Orchid not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Orchid> getOrchidById(@PathVariable Long id) {
        return orchidService.getOrchidById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Create new orchid", description = "Creates a new orchid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orchid created successfully",
                    content = @Content(schema = @Schema(implementation = Orchid.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<Orchid> createOrchid(@RequestBody Orchid orchid) {
        Orchid createdOrchid = orchidService.createOrchid(orchid);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrchid);
    }
    
    @Operation(summary = "Update orchid", description = "Updates an existing orchid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orchid updated successfully",
                    content = @Content(schema = @Schema(implementation = Orchid.class))),
            @ApiResponse(responseCode = "404", description = "Orchid not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Orchid> updateOrchid(@PathVariable Long id, @RequestBody Orchid orchid) {
        try {
            Orchid updatedOrchid = orchidService.updateOrchid(id, orchid);
            return ResponseEntity.ok(updatedOrchid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Delete orchid", description = "Deletes an orchid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orchid deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Orchid not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrchid(@PathVariable Long id) {
        try {
            orchidService.deleteOrchid(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Get orchids by category", description = "Returns orchids filtered by category")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved orchids by category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Orchid>> getOrchidsByCategory(@PathVariable Long categoryId) {
        List<Orchid> orchids = orchidService.getOrchidsByCategory(categoryId);
        return ResponseEntity.ok(orchids);
    }
    
    @Operation(summary = "Search orchids by name", description = "Returns orchids matching the search term")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching orchids")
    @GetMapping("/search")
    public ResponseEntity<List<Orchid>> searchOrchids(@RequestParam String name) {
        List<Orchid> orchids = orchidService.searchOrchidsByName(name);
        return ResponseEntity.ok(orchids);
    }
}
