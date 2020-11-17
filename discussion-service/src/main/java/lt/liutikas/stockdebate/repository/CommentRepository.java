package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.Comment;
import org.springframework.stereotype.Component;

@Component
//public interface CommentRepository extends JpaRepository<Comment, Integer> {
public class CommentRepository {
    public Comment findBySymbolIgnoreCase(String symbol) {
        return new Comment();
    }
}
