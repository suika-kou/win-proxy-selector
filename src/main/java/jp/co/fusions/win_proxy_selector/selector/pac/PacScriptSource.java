package jp.co.fusions.win_proxy_selector.selector.pac;

import java.io.IOException;

/*****************************************************************************
 * An source to fetch the PAC script from.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public interface PacScriptSource {

	String getName();

	/*************************************************************************
	 * Gets the PAC script content as String.
	 * 
	 * @return a script.
	 ************************************************************************/

	String getScriptContent() ;

	/*************************************************************************
	 * Checks if the content of the script is valid and if it is possible to use
	 * this script source for a PAC selector. Note that this might trigger a
	 * download of the script content from a remote location.
	 * 
	 * @return true if everything is fine, else false.
	 ************************************************************************/

	boolean isScriptValid();


}