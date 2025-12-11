package ext.koel.koelpart.datautility;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.dataUtilities.InfoPageStateDataUtility;
import com.ptc.core.components.rendering.guicomponents.TextDisplayComponent;
import com.ptc.core.meta.common.TypeIdentifierHelper;

import ext.koel.KoelUtil;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartMaster;
import wt.part.WTPartReferenceLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

public class KOELPartStateDU extends InfoPageStateDataUtility {

    @Override
    public Object getDataValue(String var1, Object var2, ModelContext var3) throws WTException {

        String lifecycleState = this.getLifecycleStateFromParameters(var2, var3);
        KoelUtil ku=new KoelUtil();
        String displayValue = lifecycleState;
       
        if (var2 instanceof WTPart) {
            WTPart part = (WTPart) var2;
        	String partType=TypeIdentifierHelper.getType(part).toString();
			if(partType.contains("ApplicationCode") || partType.contains("BasicCode") || partType.contains("ConfigurableApplicationCode")){
				System.out.println("=== TextDataUtility.getDataValue START ===");
	            System.out.println("Part Number: " + part.getNumber());
	            System.out.println("Part Name  : " + part.getName());
	            boolean isValidationExist = false;
	            String state=part.getState().toString();
	            if((state!=ext.koel.KoelConstants.STATE_TERMINATED)|| (state!=ext.koel.KoelConstants.STATE_OBSOLETE)) {
	            	
	            
				try {
					isValidationExist = isLinkExist(part);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            String plannerCode=(String) ku.getIBAValue(part,"plannercode");
	            String revision=part.getVersionInfo().getIdentifier().getValue();
	            System.out.println("plannerCode"+plannerCode);
	            if (("MTA\\MTO_Code".equalsIgnoreCase(plannerCode))) {

	                System.out.println("Decision: Beta (planner code matched)");
	                displayValue = lifecycleState + " \n Beta";
	                TextDisplayComponent tdc = new TextDisplayComponent(displayValue);
	                tdc.setValue(displayValue);
	                return tdc;
	            }else if(revision.equals("00") && part.getState().toString().equals(ext.koel.KoelConstants.STATE_RELEASED_TO_ERP)){
	            	displayValue = lifecycleState + " \n Alpha0";
	            	 TextDisplayComponent tdc = new TextDisplayComponent(displayValue);
	            	 tdc.setValue(displayValue);
	            	System.out.println("Decision: Alpha0");
	            	 return tdc;
				}else if(isValidationExist==true) {
					displayValue = lifecycleState + " \n Alpha1";
					 TextDisplayComponent tdc = new TextDisplayComponent(displayValue);
					 tdc.setValue(displayValue);
	            	System.out.println("Decision: Alpha1");
	            	 return tdc;
				}
	            }
			}
            
        }
    
           
       
		return super.getDataValue(var1, var2, var3);

        
    }
    
    public static boolean isLinkExist(WTPart part) throws Exception {
    	WTPartReferenceLink link=null;
    	boolean flag=false;
		QueryResult qr = PersistenceHelper.manager.navigate(part,WTPartReferenceLink.ROLE_BOBJECT_ROLE, WTPartReferenceLink.class, false);
		while (qr.hasMoreElements()) {
			link = (WTPartReferenceLink) qr.nextElement();
			System.out.println("link--"+link.getRoleBObject());
			
			WTDocumentMaster roleB = (WTDocumentMaster)link.getRoleBObject();	  
			WTDocument doc = (WTDocument)VersionControlHelper.service.allVersionsOf(roleB).nextElement();
			System.out.println("doc number"+doc.getNumber());
			String docType=TypeIdentifierHelper.getType(doc).toString();
			if(docType.contains("workOrderCRE") && doc.getState().toString().equals("RELEASED"))
			flag=true;
			
		}
		return flag;
	}

    protected boolean getReleasedCREWorkRequest(WTPart part) throws Exception {
    	QuerySpec qs=new QuerySpec(WTDocument.class);
    	//TypeDefinitionReference typeDefRef = ClientTypedUtility.getTypeDefinitionReference(internalNameModel);
		//qs.appendWhere(new SearchCondition(WTPart.class, "typeDefinitionReference.key.id", SearchCondition.EQUAL, typeDefRef.getKey().getId()), new int[] { 0, 1 });
    	TypeDefinitionReference testCellRef1=TypedUtilityServiceHelper.service.getTypeDefinitionReference("wt.doc.WTDocument|com.ptc.ReferenceDocument|COM.KIRLOSKAR.KGC.workOrderCRE");
		qs.appendWhere(new SearchCondition(WTDocument.class,"typeDefinitionReference.key.branchId","=",testCellRef1.getKey().getBranchId()),new int[] { 0});
		
    	qs.appendAnd();
		qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.IN, part.getNumber()), new int[] { 0, 1 });
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(WTDocument.class, "iterationInfo.latest", "TRUE"),new int[] {0,1});
		  
		QueryResult qr= PersistenceHelper.manager.find(qs);
		while(qr.hasMoreElements()){
			 WTDocument doc=(WTDocument)qr.nextElement();
			 System.out.println("Work Request"+doc.getNumber()+"==state:"+doc.getState());
			 
            String state = doc.getLifeCycleState().toString();

            if ("RELEASED".equalsIgnoreCase(state)) {
                System.out.println(" -> MATCH: latest iteration is RELEASED.");
                return true;
            }
        }

        System.out.println(">>> No referenced documents have latest iteration in RELEASED.");
        return false;
    }

   
}