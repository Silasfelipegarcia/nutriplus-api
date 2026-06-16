package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealResponse {
    private Long id;
    private MealType mealType;
    private String name;
    private List<MealItemResponse> items;
}
