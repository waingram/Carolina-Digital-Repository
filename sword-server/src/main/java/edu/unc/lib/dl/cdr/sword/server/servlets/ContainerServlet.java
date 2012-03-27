package edu.unc.lib.dl.cdr.sword.server.servlets;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.swordapp.server.ContainerAPI;
import org.swordapp.server.ContainerManager;
import org.swordapp.server.StatementManager;

import edu.unc.lib.dl.cdr.sword.server.SwordConfigurationImpl;

@Controller
@RequestMapping(SwordConfigurationImpl.EDIT_PATH)
public class ContainerServlet extends BaseSwordServlet {
	private static Logger LOG = Logger.getLogger(ContainerServlet.class);

	@Resource
	private ContainerManager containerManager;
	private ContainerAPI api;
	private StatementManager statementManager;

	@PostConstruct
	public void init() throws ServletException {
		statementManager = null;
		this.api = new ContainerAPI(containerManager, statementManager, this.config);
	}

	@RequestMapping(value = { "/{pid}", "/{pid}/*" }, method = RequestMethod.DELETE)
	public void deleteContainer(HttpServletRequest req, HttpServletResponse resp){
		try {
			this.api.delete(req, resp);
		} catch (Exception e) {
			LOG.error("Failed to delete container " + req.getQueryString(), e);
		}
	}
	
	@RequestMapping(value = { "/{pid}", "/{pid}/*" }, method = RequestMethod.PUT)
	public void replaceMetadataOrMetadataAndContent(HttpServletRequest req, HttpServletResponse resp){
		resp.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
	}

	public void setContainerManager(ContainerManager containerManager) {
		this.containerManager = containerManager;
	}

	public void setStatementManager(StatementManager statementManager) {
		this.statementManager = statementManager;
	}
}