package RepositoryInformation.servlets;

import chat.utils.ServletUtils;
import chat.utils.SessionUtils;
import com.google.gson.Gson;
import dataFromServlet.BranchesActionStatus;
import logic.Branch;
import logic.MAGit;
import logic.Repository;
import logic.notifications.DeleteBranchNotification;
import logic.users.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

public class DeleteBranchServlet extends HttpServlet {
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userName = SessionUtils.getUsername(request.getSession());
        UserManager gameManager = ServletUtils.getUserManager(getServletContext());
        MAGit currentMagit = gameManager.getMagitUser(userName);
        Repository localRepository = currentMagit.getActiveRepository();
        String branchNameToDelete = request.getParameter("branchNameToDelete");
        Branch branchInLocalRepository = null;


        String remoteRepositoryUserName = extractUserNameFromRemoteRepoLocation(localRepository.getM_RemoteRepositoryLocation());
        MAGit remoteRepoMagit = gameManager.getMagitUser(remoteRepositoryUserName);
        Repository remoteRepository = remoteRepoMagit.getRepositories().get(localRepository.getM_RemoteRepositoryLocation());


        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        if(branchNameToDelete == null || branchNameToDelete.isEmpty()){
            out.println(gson.toJson(new BranchesActionStatus(false,branchNameToDelete,"Cant enter empty name!")));
        }
        else if (currentMagit.isHeadBranch(branchNameToDelete)){
            out.println(gson.toJson(new BranchesActionStatus(false,branchNameToDelete,"You can not delete the head branch!")));
        }
        else if(!currentMagit.getActiveRepository().getM_Branches().containsKey(branchNameToDelete)){
            out.println(gson.toJson(new BranchesActionStatus(false,branchNameToDelete,"Can't find this branch on the Repository")));
        }
        else{
            branchInLocalRepository = currentMagit.getActiveRepository().getM_Branches().get(branchNameToDelete);
            if(branchInLocalRepository.getM_IsRemote()){
                out.println(gson.toJson(new BranchesActionStatus(false,branchNameToDelete,"Can't delete Remote branch!")));
            }
            else {
                boolean isRemoteTrackingBranch = branchInLocalRepository.getM_Tracking();
                //אם הבראנץ' הינו RTB
                if(isRemoteTrackingBranch){
                    //צריך למחוק אותו מהרפוסיטורי המרוחק בנוסף
                    Branch branchInRR = remoteRepository.getM_Branches().get(branchInLocalRepository.getM_Name());
                    remoteRepository.deleteBranch(branchInRR,!isRemoteTrackingBranch);
                    remoteRepoMagit.getNotifications().add(new DeleteBranchNotification(branchInRR,userName));
                }
                //אחרת נמחק אותו רק מהלוקלי
                localRepository.deleteBranch(branchInLocalRepository,isRemoteTrackingBranch);
                out.println(gson.toJson(new BranchesActionStatus(true,branchNameToDelete,null)));
            }
        }
        response.setStatus(200);
    }

    private String extractUserNameFromRemoteRepoLocation(String i_RemoteRepoLocation) {
        String separator = "\\";
        String[] splittedLocationContent = i_RemoteRepoLocation.split(Pattern.quote(separator));

        return splittedLocationContent[2];
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

