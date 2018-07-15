package org.doubango.ngn.events;

public enum AdhocSessionEventTypes {
	
	/** 来电 */
	INCOMING,
	
	/**呼出**/
	INPROGRESS,
	
	/**振铃**/
	RING,
	
    /** 接通  */
    INCALL,
    
    /**挂断**/
    TERMWAIT,
    
    /*PTT*/
    PTT_REQUEST
}
