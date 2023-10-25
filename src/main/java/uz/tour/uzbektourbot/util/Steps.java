package uz.tour.uzbektourbot.util;

public interface Steps {
    String REGISTERED = "REGISTERED";
    String SEND_CONTACT = "send_contact";

    String SEND_ADS = "SEND_ADS";


    //child steps

    String send_ads_photo = "send_ads_photo";
    String send_ads_text = "send_ads_text";
}
