package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.ShoppingList;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.dto.response.ShoppingListResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.ShoppingListRepository;
import br.com.nutriplus.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final CurrentUser currentUser;
    private final ShoppingListRepository shoppingListRepository;
    private final ResponseMapper responseMapper;

    public ShoppingListResponse getLatest() {
        User user = currentUser.get();
        List<ShoppingList> lists = shoppingListRepository.findByUserIdWithItemsOrderByCreatedAtDesc(user.getId());
        if (lists.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma lista de compras encontrada");
        }
        return responseMapper.toShoppingListResponse(lists.getFirst());
    }
}
