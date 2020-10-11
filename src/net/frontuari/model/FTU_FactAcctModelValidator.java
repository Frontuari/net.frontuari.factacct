package net.frontuari.model;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.compiere.acct.FactLine;
import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MPayment;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

/**
 *	Validator or Localization Colombia (Detailed Names)
 *	
 *  @author Carlos Ruiz - globalqss - Quality Systems & Solutions - http://globalqss.com 
 *	@version $Id: LCO_Validator.java,v 1.4 2007/05/13 06:53:26 cruiz Exp $
 */
public class FTU_FactAcctModelValidator extends AbstractEventHandler
{
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(FTU_FactAcctModelValidator.class);

	/**
	 *	Initialize Validation
	 */
	@Override
	protected void initialize() {
		registerTableEvent(IEventTopics.PO_AFTER_NEW, FactLine.Table_Name);
		registerTableEvent(IEventTopics.PO_AFTER_CHANGE, FactLine.Table_Name);
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	Called after PO.beforeSave/PO.beforeDelete
     *	when you called addModelChange for the table
     *  @param event
     *	@exception Exception if the recipient wishes the change to be not accept.
     */
	@Override
	protected void doHandleEvent(Event event) {
		PO po = getPO(event);
		String type = event.getTopic();
		log.info(po + " Type: " + type);
		
		if (po.get_TableName().equals(FactLine.Table_Name) && ( type.equals(IEventTopics.PO_AFTER_NEW)))
		{
			FactLine factLine = (FactLine)po;
			
			int recorId = factLine.getRecord_ID();
			int recorLineId = factLine.getLine_ID();
			MTable table = (MTable) factLine.getAD_Table();
			
			int docTypeId = 0;
			String documentNo = "";
			int chargeId = 0;
			
			PO poDoc = null;
			PO poLine = null;
			
			if(table.getTableName().equalsIgnoreCase("C_Invoice")) {
				
				poDoc = new MInvoice (po.getCtx(),recorId,po.get_TrxName());
				docTypeId = poDoc.get_ValueAsInt("C_DocType_ID");
				documentNo = poDoc.get_ValueAsString("DocumentNo");
				if(recorLineId>0) {
					poLine = new MInvoiceLine (po.getCtx(),recorLineId,po.get_TrxName());
					chargeId = poLine.get_ValueAsInt("C_Charge_ID");
				}
				
			}else if(table.getTableName().equalsIgnoreCase("M_InOut")) {
				
				poDoc = new MInOut (po.getCtx(),recorId,po.get_TrxName());
				docTypeId = poDoc.get_ValueAsInt("C_DocType_ID");
				documentNo = poDoc.get_ValueAsString("DocumentNo");
				if(recorLineId>0) {
					poLine = new MInOutLine (po.getCtx(),recorLineId,po.get_TrxName());
					chargeId = poLine.get_ValueAsInt("C_Charge_ID");
				}
				
			}else if(table.getTableName().equalsIgnoreCase("C_Payment")) {
				
				poDoc = new MPayment (po.getCtx(),recorId,po.get_TrxName());
				docTypeId = poDoc.get_ValueAsInt("C_DocType_ID");
				documentNo = poDoc.get_ValueAsString("DocumentNo");
				chargeId = poDoc.get_ValueAsInt("C_Charge_ID");
			}
			//	Support for register changes when it's M_Inventory, C_AllocationHdr
			else if(table.getTableName().equalsIgnoreCase("M_Inventory"))
			{
				poDoc = new MInventory (po.getCtx(),recorId,po.get_TrxName());
				docTypeId = poDoc.get_ValueAsInt("C_DocType_ID");
				documentNo = poDoc.get_ValueAsString("DocumentNo");
				if(recorLineId>0) {
					poLine = new MInventoryLine (po.getCtx(),recorLineId,po.get_TrxName());
					chargeId = poLine.get_ValueAsInt("C_Charge_ID");
				}
			}
			else if(table.getTableName().equalsIgnoreCase("C_AllocationHdr"))
			{
				poDoc = new MAllocationHdr (po.getCtx(),recorId,po.get_TrxName());
				docTypeId = poDoc.get_ValueAsInt("C_DocType_ID");
				documentNo = poDoc.get_ValueAsString("DocumentNo");
				if(recorLineId>0) {
					poLine = new MAllocationLine (po.getCtx(),recorLineId,po.get_TrxName());
					chargeId = poLine.get_ValueAsInt("C_Charge_ID");
				}
			}
			
			if(docTypeId>0)
				factLine.set_ValueOfColumn("C_DocType_ID", docTypeId);
			
			if(chargeId>0)
				factLine.set_ValueOfColumn("C_Charge_ID", chargeId);
			
			if(documentNo!=null || (!documentNo.equals("")))
				factLine.set_ValueOfColumn("DocumentNo", documentNo);
			
			factLine.saveEx(po.get_TrxName());
		}
	}	
}