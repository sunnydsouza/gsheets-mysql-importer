package com.sunnydsouza.gsheet;

import com.sunnydsouza.gsheet.importer.ColumnTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableTransformer {
    Map<String,ColumnTransformer> columnTransformerMap=new HashMap<>();

    public TableTransformer addColumnTransformer(String columnName, ColumnTransformer columnTransformer) {
        columnTransformerMap.put(columnName,columnTransformer);
        return this;
    }

    public List<Map<String, String>> transform(List<Map<String, String>> tableDataMap) {
        tableDataMap.stream()
                .forEach(row->{
                    row.entrySet().stream()
                            .forEach(entry->{
                                ColumnTransformer columnTransformer=columnTransformerMap.getOrDefault(entry.getKey(),null);
                                if(columnTransformer!=null) {
                                    entry.setValue((String) columnTransformer.transform(entry.getValue()));
                                }
                            });
                });
        return tableDataMap;
    }
}
