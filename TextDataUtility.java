package ext.CustomDataUtility;

import com.ptc.core.components.factory.dataUtilities.InfoPageStateDataUtility;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.lifecyclediagram.LifecycleDiagramConfig;
import com.ptc.core.components.lifecyclediagram.LifecycleDiagramUtil;
import com.ptc.core.components.rendering.guicomponents.TextDisplayComponent;
import com.ptc.core.components.rendering.guicomponents.UrlDisplayComponent;
import com.ptc.core.components.util.AttributeHelper;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.workflow.work.WorkItem;

public class TextDataUtility extends InfoPageStateDataUtility {

	public Object getDataValue(String var1, Object var2, ModelContext var3) throws WTException {
		System.out.println("111111111111##########");

		String var4 = this.getLifecycleStateFromParameters(var2, var3);
	    LifecycleDiagramConfig var5 = LifecycleDiagramUtil.getLifecycleDiagramConfig(var2);
System.out.println("22222222##########");
	    if (!StringUtils.isEmpty(var4)) {
	        String var6 = LifecycleDiagramUtil.getTooltip(var4);

	        // Append new line + SHUBHAM
	        String displayValue = var4 + "<br/>SHUBHAM";

	        if (var5 == null) {
	            TextDisplayComponent var8 = new TextDisplayComponent(var1, displayValue);
	            var8.setTooltip(var6);
	            var8.setCheckXSS(false); // allow <br/> rendering
	            System.out.println("3333333##########");
	            return var8;
	            

	        } else {
	            UrlDisplayComponent var7 = new UrlDisplayComponent(
	                    var1,
	                    displayValue,
	                    LifecycleDiagramUtil.getURLForLifeCycleDiagramConfig(var5)
	            );
	            var7.setToolTip(var6);
	            var7.setNonPureHTMLLink(true);
	            var7.setCheckXSS(false); // allow <br/> rendering
	            System.out.println("4444443##########");
	            return var7;
	        }
	    } else {
	        return TextDisplayComponent.NBSP;
	    }
	}
}
