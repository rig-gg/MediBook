package edu.cit.amihan.medibook.fda;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FdaDrugSuggestion {
    private String brandName;
    private String genericName;
    private String route;
    private String indication;
}