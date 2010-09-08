package ijx;

import java.awt.Image;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author GBH
 */
public class PassingAndReturningMaps {

    public Map run(Map inputParameters) {
        inputParameters.get("input");
        HashMap outputParamters  = new HashMap();
        outputParamters.put("output", new Integer(1));
        return  outputParamters;

    }

    public static void main(String[] args) {
        HashMap inputParameters = new HashMap();
        inputParameters.put("input", new String("Ho Ho Ho"));
        int i = (Integer) new PassingAndReturningMaps().run(inputParameters).get("output");
    }


}
