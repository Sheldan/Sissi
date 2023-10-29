package dev.sheldan.sissi.module.rssnews.repository;

import dev.sheldan.sissi.module.rssnews.model.database.NewsPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsPostRepository extends JpaRepository<NewsPost, Long> {
}
