package com.nol.ivan.little_apple2.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nol.ivan.little_apple2.MainActivity;
import com.nol.ivan.little_apple2.R;

/**
 * Created by Ivan on 2016/12/25.
 */

public class ReadmeFragment extends Fragment{
   // TextView ReadMe ;
    TextView tv1_title;
    TextView tv1;
    TextView tv2_title;
    TextView tv2;
    TextView copyright;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_readme, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Readme");

//        ReadMe = (TextView) view.findViewById(R.id.ReadMeText);
//        ReadMe.setText("1.    請先連接藍芽耳機和Koala感測器\n" +
//                "2.    連接感測器後，請依指示執行校正球拍動作\n" +
//                "3.    以上動作結束後至Menu選單裡點選VoicePrint，請依指示執行\n" +
//                "4.    如之前做過VoicePrint校正，可跳過重點3\n" +
//                "5.    如需重新校正球拍或聲紋，可至Menu選單內自行點選");
//        ReadMe.setTextSize(25);

        tv1_title = (TextView) view.findViewById(R.id.tv1_title);
        tv1_title.setText("[免責條款]\n");
        tv1 = (TextView) view.findViewById(R.id.tv1);
        tv1.setText("您必須自行承擔使用SmartRecord之風險，在任何情況下，開發團隊對於使用或無法使用SmartRecord各項服務造成任何損失，一概免責。\n");
        tv2_title = (TextView) view.findViewById(R.id.tv2_title);
        tv2_title.setText("[個人資料保護法]\n");
        tv2 = (TextView) view.findViewById(R.id.tv2);
        tv2.setText("SmartRecord 開發團隊，依「個人資料保護法」，作為本團隊蒐集、處理及利用您提供與本團隊個人資料之準繩。您理解並同意，將在為您提供服務期間蒐集您的個人資料\n" +
                "依照個人資料保護法保護法第8條規定進行蒐集前之告知：\n" +
                "1.機關名稱：國立交通大學-網路最佳化實驗室。\n" +
                "2.蒐集目的：提供本團隊進行學術分析統計研究。\n" +
                "3.個人資料類別：用戶操作本服務時紀錄的文字資訊。\n" +
                "4.個人資料利用期間：本團隊進行學術研究所必須之保存期間。\n" +
                "個人資料利用對象：本團體及合作學術夥伴。\n" +
                "個人資料利用方式：依蒐集目的範圍及此隱私權政策之利用。\n" +
                "5.依個人資料保護法第3條規定，您享有查詢或請求閱覽、請求製給複製本、請求補充或更正、請求停止蒐集、處理或利用、請求刪除之權利。您可以透過寄送電子郵件至nol.nctucs@gmail.com的方式行使以上權利，本團隊在收悉您的請求後將盡速處理。\n" +
                "6.您若不提供個人資料所致權益之影響：在操作本軟體期間，及代表您同意提供相關個人資料，您若拒絕提供，可將此軟體解除安裝，若您需行使個人資料保護法第3條之權利，可參考第3點說明\n");
        copyright = (TextView) view.findViewById(R.id.copyright);
        copyright.setText("Copyright © 2016-2017\n" +
                "國立交通大學-網路最佳化實驗室\n" +
                "NCTU - Network Optimization Lab");




        return view;
    }
}
