/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.accountorganisation.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectInvoiceService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectInvoiceService.class);

	@Inject
	private TaskInvoiceService taskInvoiceService;


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(Project project) throws AxelorException {

		// Check if the fields clientPartner, contactPartner and company are not empty
		if(project.getClientPartner() != null && project.getContactPartner() != null && project.getCompany() != null) {

			Invoice invoice = this.createInvoice(project);
			invoice.save();
			return invoice;
		}
		return null;
	}

	public Invoice createInvoice(Project project) throws AxelorException {

		
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator(IInvoice.CLIENT_SALE, project.getCompany(), project.getClientPartner(), project.getContactPartner(), null) {

			@Override
			public Invoice generate() throws AxelorException {

				return super.createInvoiceHeader();
			}
		};

		Invoice invoice = invoiceGenerator.generate();
		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, project));
		invoiceGenerator.computeInvoice(invoice);
		return invoice;
	}
	
	
	@Transactional
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, Project project) throws AxelorException  {
		
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		
		for(Task task : project.getTaskList()) {
			
			if(task.getIsToInvoice() && task.getSalesOrderLine() != null) {
				
				invoiceLineList.addAll(taskInvoiceService.createInvoiceLine(invoice, task));
			}
		}
		return invoiceLineList;	
	}
	
}
