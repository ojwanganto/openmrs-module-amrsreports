package org.openmrs.module.amrsreports.tags;

import org.openmrs.module.amrsreports.QueuedReport;


import java.util.Map;

/**
 * This is a util class for searching through a collection and should be used with JSPs
 *
 */
public class Util {
    public static String intervalunit(Map<QueuedReport,String> coll, QueuedReport o) {
        if(coll.containsKey(o)){
            return coll.get(o);
        }
        return "No Repeat";

    }
}
