package servlet;

import dao.ReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class ReportServlet extends HttpServlet {
    private ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) action = "dashboard";

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        switch (action) {
            case "revenue":
                showRevenue(request, response, currentYear, currentMonth);
                break;
            case "tickets":
                showTickets(request, response);
                break;
            case "occupancy":
                showOccupancy(request, response);
                break;
            case "top10":
                showTop10(request, response, currentYear, currentMonth);
                break;
            case "customers":
                showTopCustomers(request, response);
                break;
            case "summary":
                showSummary(request, response);
                break;
            default:
                showDashboard(request, response, currentYear, currentMonth);
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response,
                                int year, int month) throws ServletException, IOException {
        // Doanh thu tháng hiện tại
        List<Map<String, Object>> monthlyRevenue =
            reportDAO.getRevenueByPeriod("month", year, null);

        // Top 10 phim tháng này
        List<Map<String, Object>> top10 = reportDAO.getTop10MoviesByRevenue(year, month);

        // Tổng kết 6 tháng
        Map<String, Object> summary = reportDAO.getSummaryReport(6);

        // Top khách hàng
        List<Map<String, Object>> topCustomers = reportDAO.getTopCustomersByPoints(5);

        request.setAttribute("monthlyRevenue", monthlyRevenue);
        request.setAttribute("top10", top10);
        request.setAttribute("summary", summary);
        request.setAttribute("topCustomers", topCustomers);
        request.setAttribute("currentYear", year);
        request.setAttribute("currentMonth", month);
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }

    private void showRevenue(HttpServletRequest request, HttpServletResponse response,
                              int currentYear, int currentMonth) throws ServletException, IOException {
        String period = request.getParameter("period");
        if (period == null) period = "month";

        String yearStr = request.getParameter("year");
        String monthStr = request.getParameter("month");
        int year = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr) : currentYear;
        Integer month = (monthStr != null && !monthStr.isEmpty()) ? Integer.parseInt(monthStr) : null;

        List<Map<String, Object>> data = reportDAO.getRevenueByPeriod(period, year, month);

        request.setAttribute("revenueData", data);
        request.setAttribute("selectedPeriod", period);
        request.setAttribute("selectedYear", year);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("currentYear", currentYear);
        request.setAttribute("currentMonth", currentMonth);
        request.setAttribute("activeTab", "revenue");
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }

    private void showTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> byMovie = reportDAO.getTicketsByMovie();
        List<Map<String, Object>> byShowtime = reportDAO.getTicketsByShowtime();

        request.setAttribute("ticketsByMovie", byMovie);
        request.setAttribute("ticketsByShowtime", byShowtime);
        request.setAttribute("activeTab", "tickets");
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }

    private void showOccupancy(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> data = reportDAO.getOccupancyByRoom();
        request.setAttribute("occupancyData", data);
        request.setAttribute("activeTab", "occupancy");
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }

    private void showTop10(HttpServletRequest request, HttpServletResponse response,
                            int currentYear, int currentMonth) throws ServletException, IOException {
        String yearStr = request.getParameter("year");
        String monthStr = request.getParameter("month");
        int year = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr) : currentYear;
        int month = (monthStr != null && !monthStr.isEmpty()) ? Integer.parseInt(monthStr) : currentMonth;

        List<Map<String, Object>> data = reportDAO.getTop10MoviesByRevenue(year, month);

        request.setAttribute("top10Data", data);
        request.setAttribute("selectedYear", year);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("currentYear", currentYear);
        request.setAttribute("currentMonth", currentMonth);
        request.setAttribute("activeTab", "top10");
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }

    private void showTopCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> data = reportDAO.getTopCustomersByPoints(20);
        request.setAttribute("topCustomers", data);
        request.setAttribute("activeTab", "customers");
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }

    private void showSummary(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> summary = reportDAO.getSummaryReport(6);
        List<Map<String, Object>> top10 = reportDAO.getTop10MoviesByRevenue(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH) + 1
        );
        List<Map<String, Object>> vipCustomers = reportDAO.getTopCustomersByPoints(50);

        request.setAttribute("summary", summary);
        request.setAttribute("top10", top10);
        request.setAttribute("vipCustomers", vipCustomers);
        request.setAttribute("activeTab", "summary");
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }
}