package patmob.plugin.patbase;

import java.util.Calendar;

/**
 * Specify which members (patent country and publication date) of the PatBase 
 * family are included in PatMOb results for the Query operation. 
 * @author Piotr
 */
public class PatBaseQueryResultFormat {
    boolean allCountries = false,
            allDates = false;
    String selectCountries = "US EP WO",
            afterDate = "";
    
    /**
     * Default constructor for Patent Alerts. Calculates afterDate to 2 weeks
     * before today.
     */
    public PatBaseQueryResultFormat() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -14);
        int year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH),   //January = 0
                day = calendar.get(Calendar.DAY_OF_MONTH);
        afterDate = Integer.toString(year);
        if (month+1<10) {
            afterDate = afterDate.concat("0").concat(Integer.toString(month+1));
        } else {
            afterDate = afterDate.concat(Integer.toString(month+1));
        }
        if (day<10) {
            afterDate = afterDate.concat("0").concat(Integer.toString(day));
        } else {
            afterDate = afterDate.concat(Integer.toString(day));
        }        
    }
    
    /**
     * Specify which countries and cutoff date to return. Use null for all.
     * @param coutries
     * @param date 
     */
    public PatBaseQueryResultFormat(String countries, String date) {
        if (countries==null) {
            allCountries = true;
        } else {
            selectCountries = countries;
        }
        if (date==null) {
            allDates = true;
        } else {
            afterDate = date;
        }
    }
    
    public boolean allCountries() {
        return allCountries;
    }
    
    public boolean allDates() {
        return allDates;
    }
    
    public String selectCountries() {
        return selectCountries;
    }
    
    public String afterDate() {
        return afterDate;
    }
}
