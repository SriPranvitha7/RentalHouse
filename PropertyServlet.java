package com.housefinder;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.*;
import java.util.List;

@WebServlet("/api/properties")
public class PropertyServlet extends HttpServlet {

    private PropertyDAO dao = new PropertyDAO();

    // ── GET — fetch all or search properties ─────────────────
    // Called by: script.js when map loads or search is run
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Access-Control-Allow-Origin", "*"); // allow frontend to call

        String location = req.getParameter("location") != null ? req.getParameter("location") : "";
        String type     = req.getParameter("type")     != null ? req.getParameter("type")     : "";
        String rentStr  = req.getParameter("rent")     != null ? req.getParameter("rent")     : "";

        List<Property> list;

        // If no filters, return all properties
        if (location.isEmpty() && type.isEmpty() && rentStr.isEmpty()) {
            list = dao.getAllProperties();
        } else {
            // Parse rent range e.g. "5000-10000"
            int minRent = 0;
            int maxRent = 999999;

            if (!rentStr.isEmpty()) {
                String[] parts = rentStr.split("-");
                minRent = Integer.parseInt(parts[0]);
                maxRent = Integer.parseInt(parts[1]);
            }
            list = dao.searchProperties(location, type, minRent, maxRent);
        }

        // Build JSON array and send
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            json.append(list.get(i).toJSON());
            if (i < list.size() - 1) json.append(",");
        }
        json.append("]");

        res.getWriter().write(json.toString());
    }

    // ── POST — add a new property ─────────────────────────────
    // Called by: script.js when owner submits the add property form
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Access-Control-Allow-Origin", "*");

        try {
            String owner    = req.getParameter("owner");
            String phone    = req.getParameter("phone");
            String location = req.getParameter("location");
            String address  = req.getParameter("address");
            int    rent     = Integer.parseInt(req.getParameter("rent"));
            String type     = req.getParameter("type");
            double lat      = Double.parseDouble(req.getParameter("lat"));
            double lng      = Double.parseDouble(req.getParameter("lng"));

            Property p = new Property(owner, phone, location, address, rent, type, lat, lng);
            boolean success = dao.addProperty(p);

            if (success) {
                res.getWriter().write("{\"status\":\"success\",\"message\":\"Property added\"}");
            } else {
                res.setStatus(500);
                res.getWriter().write("{\"status\":\"error\",\"message\":\"Failed to add property\"}");
            }

        } catch (Exception e) {
            res.setStatus(400);
            res.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid data: " + e.getMessage() + "\"}");
        }
    }

    // ── OPTIONS — needed for CORS preflight ──────────────────
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type");
        res.setStatus(200);
    }
}