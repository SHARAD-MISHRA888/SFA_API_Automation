package endpoints;

import config.ConfigManager;

public class Endpoints {

    // Base URL from config
    public static final String BASE_URL = ConfigManager.get("base.url");
    // create plan in bulk
    public static final String Create_Plan_In_Bulk = "/combine-tour-plan/createCombineTourPlanInBulk";
    //getAll plan id's between start and end date
    public static final String Fetch_Plan_ID = "/combine-tour-plan/findByStartAndEndDateByMemberIdForBjpAndDjpAndCjp/";
    // Approve Plans
    public static final String Approve_Plans = "/combine-tour-plan/updateApprovalStatus";
    // Upload Attendance Image
    public static final String Upload_Attendance_Image = "/uploadBase64";
    // Marl Attendance
    public static final String Mark_Attendance = "/attendance";
    // Update Vehicle Type
    public static final String Update_Vehicle_Type = "/combine-tour-plan/updateVehicleTypeByMemberId";
    // Get Today Journey Plan By Visited Date & Member ID
    public static final String Get_Today_Plan = "/combine-tour-plan/getTodayCombinePlanByMemberId/{memberId}";
    // Update Distance
    public static final String Update_Distance = "/combine-tour-plan/updateBjpAndCjpAndDjpDistance";
    // Generate Expense
    public static final String Generate_Expense = "/createExpenseWithModeOfTravelAndDA";
    // Sync Member Logs
    public static final String Sync_Member_Logs ="/sync/syncMemberLogs";
    // Update Working With
    public static final String Update_Working_with = "/combine-tour-plan/updateWorkingWithForBjpAndDjpAndCjp";
    // Fetch Product Inventory By Client ID
    public static final String Fetch_Product_Inventory = "/inventory-service/getAllInventoryByClinetFmcgId/";
    // Fetch Sample Inventory By Member ID
    public static final String Fetch_Sample_Inventory = "/inventory-service/sample-inventory/getAllSampleMemberById/";
    // Fetch All Products
    public static final String Fetch_All_Product = "/product-service/products/all";

}
