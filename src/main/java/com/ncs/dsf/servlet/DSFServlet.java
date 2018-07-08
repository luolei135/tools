package com.ncs.dsf.servlet;

import com.google.gson.Gson;
import com.ncs.dsf.controller.Controller;
import com.ncs.dsf.core.RequestContextHolder;
import com.ncs.dsf.exception.ToolException;
import com.ncs.dsf.utils.DBUtil;
import com.ncs.dsf.vo.AuditVO;
import com.ncs.dsf.vo.SyncVO;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class DSFServlet extends HttpServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
//        super.service(req, res);
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        Controller controller = new Controller();
        Method method = null;
        method = getMethod(path, method);
        request.getParameterMap();
        request.getPathInfo();
        request.getHeaderNames();

        Connection newCon = new DBUtil().getMetaConn(new SyncVO(), true);
        RequestContextHolder.getDSFRequestContext().setConnection(newCon);
        try {
            newCon.setAutoCommit(false);

//        newCon.prepareStatement(null).;
            if (method != null) {

                Object obj = method.invoke(controller, prepareSyncVo(getRequestPayload(request), new SyncVO()));
                RequestContextHolder.getDSFRequestContext().getConnection().commit();
                if (obj != null) {
                    response.getWriter().write(obj.toString());
                }

            } else {
                response.sendError(404);
                response.getWriter().write("method not found.");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }  catch (InvocationTargetException e) {
            e.printStackTrace();
            if(e.getTargetException() instanceof ToolException){
                response.sendError(500,e.getTargetException().getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(500);
            response.getWriter().write("system error:0001");
        }finally {
            try {
                RequestContextHolder.getDSFRequestContext().getConnection().rollback();
                RequestContextHolder.getDSFRequestContext().getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    private SyncVO prepareSyncVo(String data, SyncVO vo) {
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(data);
            vo.setCustomerId((String) jsonObject.get("customerId"));
            vo.setSyncDownCase((String) jsonObject.get("syncDownCase"));
            vo.setCaseId((String) jsonObject.get("caseId"));
            vo.setSyncUpCase((String) jsonObject.get("syncUpCase"));
            vo.setRootFolder((String) jsonObject.get("rootFolder"));
            vo.setSyncDownServer((String) jsonObject.get("syncDownServer"));
            vo.setSyncUpServer((String) jsonObject.get("syncUpServer"));
            if (jsonObject.get("selectedAudit") != null)
                vo.setSelectAudit(new Gson().fromJson(jsonObject.get("selectedAudit").toString(), AuditVO.class));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return vo;
    }

    private String getRequestPayload(HttpServletRequest req) {

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader();) {
            char[] buff = new char[1024];
            int len;
            while ((len = reader.read(buff)) != -1) {
                sb.append(buff, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private Method getMethod(String path, Method method) {
        try {
            method = Controller.class.getDeclaredMethod(path.replace("/controller/", ""), new Class[]{SyncVO.class});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }
}
