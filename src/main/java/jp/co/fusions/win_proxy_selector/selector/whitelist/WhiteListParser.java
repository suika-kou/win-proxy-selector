package jp.co.fusions.win_proxy_selector.selector.whitelist;

import java.util.List;

import jp.co.fusions.win_proxy_selector.util.UriFilter;

/*****************************************************************************
 * Interface for an white list parser. This will take an white list string and
 * parse it into a list of UriFilter rules.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public interface WhiteListParser {

	/*************************************************************************
	 * Parses a list of host name and IP filters into UriFilter objects.
	 * 
	 * @param whiteList
	 *            the string to parse.
	 * @return a list of UriFilters
	 ************************************************************************/

	public List<UriFilter> parseWhiteList(String whiteList);

}
