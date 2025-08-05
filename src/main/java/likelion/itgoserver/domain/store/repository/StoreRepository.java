package likelion.itgoserver.domain.store.repository;

import likelion.itgoserver.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}