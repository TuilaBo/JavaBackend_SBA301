package service;

import pojo.Orchid;
import java.util.List;
import java.util.Optional;

public interface OrchidService {
    List<Orchid> getAllOrchids();
    Optional<Orchid> getOrchidById(Long id);
    Orchid createOrchid(Orchid orchid);
    Orchid updateOrchid(Long id, Orchid orchid);
    void deleteOrchid(Long id);
    List<Orchid> getOrchidsByCategory(Long categoryId);
    List<Orchid> searchOrchidsByName(String name);
}
