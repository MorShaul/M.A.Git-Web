package RepositoryInformation.servlets;

import chat.utils.ServletUtils;
import chat.utils.SessionUtils;
import com.google.gson.Gson;
import dataFromServlet.ChangedFileAfterPullRequest;
import dataFromServlet.ChangedInformationAfterPullRequest;
import logic.*;
import logic.pullRequest.PullRequest;
import logic.users.UserManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ChangedFileServlet extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        String userName = SessionUtils.getUsername(request.getSession());
        UserManager gameManager = ServletUtils.getUserManager(getServletContext());
        MAGit currentMagit = gameManager.getMagitUser(userName);
        Repository localRepository = currentMagit.getActiveRepository();

        String CommitSHA1ToCheck = request.getParameter("commitSHA1");
        Commit commitToCheck = localRepository.getM_Commits().get(CommitSHA1ToCheck);
        Commit commitToCompareWith = localRepository.getM_Commits().get(commitToCheck.getFirstPrecedingCommitId());

        List<FileFullPathAndItemData> addedFiles = new ArrayList<>();
        List<FileFullPathAndItemData> deletedFiles = new ArrayList<>() ;
        List<FileFullPathAndItemData> updatedFiles = new ArrayList<>();

        currentMagit.CompareTwoCommitsForPR(commitToCheck, commitToCompareWith, addedFiles, deletedFiles, updatedFiles, localRepository);

        out.println(gson.toJson(new ChangedFileAfterPullRequest(addedFiles, deletedFiles, updatedFiles)));
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}

