package com.example.lab4;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.util.*;

@WebServlet("/Cookies")
public class CookiesLab4 extends HttpServlet {
    private static final String UNIQ_ID = "lastVisit";
    private static final String USER = "user";
    private static final AtomicInteger counter = new AtomicInteger();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession();
        UserDto user;
        user = (UserDto) session.getAttribute(USER);
        if (user == null) {
            user = new UserDto(25L, "test@gmail.com");
            session.setAttribute(USER, user);
        }

        var browser = getBrowser(req.getHeader("User-Agent"));
        var ipAddress = req.getRemoteAddr();
        var lastVisitDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        var cookies = req.getCookies();
        var writer = resp.getWriter();
        resp.setContentType("text/html");

        if (Arrays.stream(cookies)
                .filter(cookie -> UNIQ_ID.equals(cookie.getName()))
                .findFirst()
                .isEmpty()) {
            var cookie = new Cookie(UNIQ_ID, lastVisitDate);
            cookie.setMaxAge(-1);
            resp.addCookie(cookie);
            counter.incrementAndGet();

            writer.write("<h1>Приветствую, вы впервые зашли на сайт!</h1>");
            var PathToFile = Paths.get("C:\\Users\\User\\IdeaProjects\\Lab4\\visitors.txt");
            Files.writeString(PathToFile, lastVisitDate+" "+ipAddress+" "+browser+"\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        else {
            writer.write("<h1>Здравствуйте, с возвращение на сайт!</h1>");
            String cookieValue;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(UNIQ_ID)) {
                    cookieValue = cookie.getValue();
                    cookie.setValue(lastVisitDate);
                    writer.write("<p>Дата последнего посещения: " + cookieValue + ".</p>");
                    writer.write("<p>Ваш браузер: " + browser + ".</p>");
                    break;
                }
            }
        }

        writer.write("<h1> Количество уникальных пользователей:  " + counter.get() + "</h1>");

        System.out.println("HttpServletRequest Атрибуты");
        printAttributes(req);
        System.out.println("ServletContext Атрибуты");
        printAttributes(req.getServletContext());
        System.out.println("HttpSession Атрибуты");
        printAttributes(req.getSession());
    }

    private static String getBrowser(String userAgent) {

        if (userAgent.contains("YaBrowser")) return "Yandex Browser";
        else if (userAgent.contains("Chrome")) return "Google Chrome";
        else return "Unknown";
    }

    private static void printAttributes(Object object) {
        if (object != null) {
            var context = object instanceof ServletContext ? (ServletContext) object : null;
            var session = object instanceof HttpSession ? (HttpSession) object : null;
            var request = object instanceof HttpServletRequest ? (HttpServletRequest) object : null;

            Enumeration<String> attributeNames = null;
            if (context != null) attributeNames = context.getAttributeNames();
            else if (session != null) attributeNames = session.getAttributeNames();
            else if (request != null) attributeNames = request.getAttributeNames();
            if (!attributeNames.hasMoreElements()) System.out.println("Нет атрибутов");
            while (attributeNames != null && attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                Object attributeValue = null;
                if (context != null) attributeValue = context.getAttribute(attributeName);
                else if (session != null) attributeValue = session.getAttribute(attributeName);
                else if (request != null) attributeValue = request.getAttribute(attributeName);
                System.out.println(attributeName + " = " + attributeValue);
            }
        }
    }
}