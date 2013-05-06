/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.typeRace;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.grizzly.websockets.WebSocketEngine;

/**
 *
 * @author vivganes
 */
public class TypeRaceAppRegistrationServlet extends HttpServlet {

private TypeRaceApp app = new TypeRaceApp();
   
    @Override
    public void init(ServletConfig config) throws ServletException {
      WebSocketEngine.getEngine().register(config.getServletContext().getContextPath()+"/type", app);             
    }
    }

 

