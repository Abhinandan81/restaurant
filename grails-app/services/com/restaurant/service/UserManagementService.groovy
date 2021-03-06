package com.restaurant.service

import com.constants.CodeConstants
import com.restaurant.domain.auth.Role
import com.restaurant.domain.auth.User
import com.restaurant.domain.auth.UserRole
import com.utils.ServiceContext
import grails.transaction.Transactional

@Transactional
class UserManagementService {
    def commonUtilService

    void bootstrapSystemRoles(){
        /* Create the SUPER_ADMIN user role. */
        if(!Role.findByAuthority(CodeConstants.ROLE_SUPER_ADMIN)) {
            def superAdminRole = new Role(authority: CodeConstants.ROLE_SUPER_ADMIN).save(flush: true, failOnError: true)
        }

        /* Create the ADMIN user role. */
        if(!Role.findByAuthority(CodeConstants.ROLE_ADMIN)) {
            def adminRole = new Role(authority: CodeConstants.ROLE_ADMIN).save(flush: true)
        }


        /* Create a super admin user. */
        User superAdmin = User.findByUsername(CodeConstants.SUPER_ADMIN_USER_NAME)
        if(!superAdmin) {
            superAdmin = new User(
                    username        :   CodeConstants.SUPER_ADMIN_USER_NAME,
                    password        :   CodeConstants.SUPER_ADMIN_USER_NAME,
                    firstName       :   "Abhianndan",
                    lastName        :   "Satpute",
                    contactNumber    :   "8796105046",
                    restaurantId    :   "1",
                    branchId        :   null,
                    enabled         :   true
            )
            superAdmin.save(flush: true, failOnError: true)
            def superAdminRole = Role.findByAuthority(CodeConstants.ROLE_SUPER_ADMIN)

            UserRole superAdminUserRole = UserRole.get(superAdmin.id, superAdminRole.id)
            if(!superAdminUserRole) {
                UserRole.create(superAdmin, superAdminRole, true)
            }
        }
    }

    User findUserByUsername(ServiceContext sCtx, String userName) {
        return User.findByUsername(userName)
    }

    /**
     * New user creation
     * @param userName
     * @param password
     * @param firstName
     * @param lastName
     * @param mobileNumber
     * @param roleOfUser
     * @return : userCreationStatusMap
     */
    Map newUserCreation(String userName, String password, String firstName, String lastName, String contactNumber,
                        String roleOfUser, String restaurantId, String branchId){
        Map userCreationStatusMap  =   [:]
        try {

            User user   =   User.findByUsername(userName)

            if (user){
                userCreationStatusMap  << [status : false, message : "User with the username ${userName} already exist"]
            }else{
                user     =   new User(username: userName, password: password, firstName : firstName,
                        lastName : lastName, contactNumber : contactNumber, restaurantId: restaurantId, branchId: branchId)
                user.save(flush: true, failOnError: true)

                def role = Role.findByAuthority(roleOfUser)
                UserRole  userRole = UserRole.get(user.id, role.id)

                if(!userRole) {
                    UserRole.create(user, role, true)
                }
                userCreationStatusMap  << [status : true, message : "User ${firstName} ${lastName} created successfully"]
            }
            return userCreationStatusMap
        }catch (Exception e){
            println "Error in new user creation"+e
        }
    }

    /**
     * Updating the existing user information
     * @return : User information update status map
     */
    Map updateUserInformation(String userId, Map detailsToUpdateMap){
        Map userUpdateStatusMap =   [:]

        try {
            User user   =   User.findById(userId)
            if (user){
                detailsToUpdateMap.each { key, value ->
                    user."${key}"   =   value
                }

                user.save(flush: true, failOnError: true)
                userUpdateStatusMap <<  [status: true, message: "User details updated successfully"]
                return userUpdateStatusMap
            }else {
                userUpdateStatusMap <<  [status: false, message: "User not found"]
                return userUpdateStatusMap
            }
        }catch (Exception e){
            println "Error while updating user information"+e.printStackTrace()
        }
    }

    /**
     * delete user
     * @param userId
     * @return : userDeletionStatusMap
     */
    Map deleteUser(String userId){
        Map userDeletionStatusMap   =   [:]
        try {
            User user   =   User.findById(userId)

            if (user){
                def userRoles   =   UserRole.findAllByUser(user)
                userRoles*.delete()
                user.delete()
                userDeletionStatusMap   <<  [status: true, message: "User deleted successfully"]
                return userDeletionStatusMap
            }else {
                userDeletionStatusMap   <<  [status: false, message: "User you are trying to delete does not exist"]
                return userDeletionStatusMap
            }

        }catch (Exception e){
            println "Error while deleting the user"
        }
    }

    List fetchAllUsersByRestaurantId(String restaurantId){
        List allUserDetailsList =   []
        List userDetailsList    =   []
        String branchName

        try {
            List users = User.findAllByRestaurantId(restaurantId)
            if (users){
                users.each { user->
                    branchName = ""

                    //to neglet super admin, check for user whoo have branch_id associated with it
                    if (user.branchId != null){
                        branchName = commonUtilService.fetchBranchNameByBranchId(user.branchId)
                        if (branchName != ""){
                            userDetailsList = [branchName,user.username, user.firstName, user.lastName,
                                               user.contactNumber, user.id, user.branchId]
                            allUserDetailsList << userDetailsList
                        }
                    }
                }
                return allUserDetailsList
            }
        }catch (Exception e){
            println "Error in fetching user details by restaurant ID"+e.printStackTrace()
        }
    }
}
