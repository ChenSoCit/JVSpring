package TayJVLearn.StartJV.Demo.util;

public interface AppConst {


    String SEARCH_OPERATOR = "(\\w+?)(:|<|>)(.*)";
    String SEARCH_SPEC_OPERATOR = "(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)";
    String SORT_BY = "(\\w+?)(:)(.*)";

}
