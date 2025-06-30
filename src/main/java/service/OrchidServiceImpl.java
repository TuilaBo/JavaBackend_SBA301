package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.Orchid;
import pojo.Category;
import repository.OrchidRepository;
import repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class OrchidServiceImpl implements OrchidService {

    @Autowired
    private OrchidRepository orchidRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Orchid> getAllOrchids() {
        return orchidRepository.findAll();
    }

    @Override
    public Optional<Orchid> getOrchidById(Long id) {
        return orchidRepository.findById(id);
    }

    @Override
    public Orchid createOrchid(Orchid orchid) {
        // Validate category exists
        if (orchid.getCategory() != null && orchid.getCategory().getCategoryId() != null) {
            Category category = categoryRepository.findById(orchid.getCategory().getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            orchid.setCategory(category);
        }
        return orchidRepository.save(orchid);
    }

    @Override
    public Orchid updateOrchid(Long id, Orchid orchid) {
        Orchid existingOrchid = orchidRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orchid not found with id: " + id));
        
        // Update fields
        existingOrchid.setOrchidName(orchid.getOrchidName());
        existingOrchid.setOrchidDescription(orchid.getOrchidDescription());
        existingOrchid.setOrchidUrl(orchid.getOrchidUrl());
        existingOrchid.setPrice(orchid.getPrice());
        existingOrchid.setNatural(orchid.getNatural());
        
        // Update category if provided
        if (orchid.getCategory() != null && orchid.getCategory().getCategoryId() != null) {
            Category category = categoryRepository.findById(orchid.getCategory().getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            existingOrchid.setCategory(category);
        }
        
        return orchidRepository.save(existingOrchid);
    }

    @Override
    public void deleteOrchid(Long id) {
        if (!orchidRepository.existsById(id)) {
            throw new IllegalArgumentException("Orchid not found with id: " + id);
        }
        orchidRepository.deleteById(id);
    }

    @Override
    public List<Orchid> getOrchidsByCategory(Long categoryId) {
        return orchidRepository.findByCategoryCategoryId(categoryId);
    }

    @Override
    public List<Orchid> searchOrchidsByName(String name) {
        return orchidRepository.findByOrchidNameContainingIgnoreCase(name);
    }
}
