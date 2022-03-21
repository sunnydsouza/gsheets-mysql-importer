package com.sunnydsouza.gsheet.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class RecordedTimestampTransformer implements ColumnTransformer<String> {
    Logger logger= LoggerFactory.getLogger(RecordedTimestampTransformer.class);
    @Override
    public String transform(String dateTimeString) {
        //Convert date from dd/MM/yyyy to yyyy-MM-dd
        String transformedDateTimeString = null;
        try {
            transformedDateTimeString= new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(dateTimeString));
            logger.debug("Transformed dateTimeString using custom logic: "+transformedDateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return transformedDateTimeString;
    }
}
