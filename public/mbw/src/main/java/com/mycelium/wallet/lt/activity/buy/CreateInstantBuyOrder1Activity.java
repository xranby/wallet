/*
 * Copyright 2013 Megion Research and Development GmbH
 *
 * Licensed under the Microsoft Reference Source License (MS-RSL)
 *
 * This license governs use of the accompanying software. If you use the software, you accept this license.
 * If you do not accept the license, do not use the software.
 *
 * 1. Definitions
 * The terms "reproduce," "reproduction," and "distribution" have the same meaning here as under U.S. copyright law.
 * "You" means the licensee of the software.
 * "Your company" means the company you worked for when you downloaded the software.
 * "Reference use" means use of the software within your company as a reference, in read only form, for the sole purposes
 * of debugging your products, maintaining your products, or enhancing the interoperability of your products with the
 * software, and specifically excludes the right to distribute the software outside of your company.
 * "Licensed patents" means any Licensor patent claims which read directly on the software as distributed by the Licensor
 * under this license.
 *
 * 2. Grant of Rights
 * (A) Copyright Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free copyright license to reproduce the software for reference use.
 * (B) Patent Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free patent license under licensed patents for reference use.
 *
 * 3. Limitations
 * (A) No Trademark License- This license does not grant you any rights to use the Licensor’s name, logo, or trademarks.
 * (B) If you begin patent litigation against the Licensor over patents that you think may apply to the software
 * (including a cross-claim or counterclaim in a lawsuit), your license to the software ends automatically.
 * (C) The software is licensed "as-is." You bear the risk of using it. The Licensor gives no express warranties,
 * guarantees or conditions. You may have additional consumer rights under your local laws which this license cannot
 * change. To the extent permitted under your local laws, the Licensor excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.mycelium.wallet.lt.activity.buy;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.mrd.bitlib.model.Address;
import com.mycelium.lt.api.model.SellOrderSearchItem;
import com.mycelium.wallet.MbwManager;
import com.mycelium.wallet.NumberEntry;
import com.mycelium.wallet.NumberEntry.NumberEntryListener;
import com.mycelium.wallet.R;
import com.mycelium.wallet.lt.activity.SendRequestActivity;
import com.mycelium.wallet.lt.api.CreateInstantBuyOrder;

public class CreateInstantBuyOrder1Activity extends Activity implements NumberEntryListener {

   public static void callMe(Activity currentActivity, SellOrderSearchItem sellOrderSearchItem) {
      Intent intent = new Intent(currentActivity, CreateInstantBuyOrder1Activity.class);
      intent.putExtra("sellOrderSearchItem", sellOrderSearchItem);
      currentActivity.startActivity(intent);
   }

   private SellOrderSearchItem _sellOrderSearchItem;

   private NumberEntry _numberEntry;
   protected MbwManager _mbwManager;
   private TextView _tvAmount;
   private Button _btStartTrading;

   /**
    * Called when the activity is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      this.requestWindowFeature(Window.FEATURE_NO_TITLE);
      this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.lt_create_instant_buy_order_1_activity);

      _mbwManager = MbwManager.getInstance(getApplication());

      // Get intent parameters
      _sellOrderSearchItem = (SellOrderSearchItem) getIntent().getSerializableExtra("sellOrderSearchItem");

      // Get intent parameters
      Integer amount = null;

      // Load saved state
      if (savedInstanceState != null) {
         amount = (Integer) savedInstanceState.getSerializable("amount");
      }

      String numberString;
      if (amount != null) {
         numberString = amount.toString();
         ((TextView) findViewById(R.id.tvAmount)).setText(numberString);
      } else {
         numberString = "";
      }

      _numberEntry = new NumberEntry(0, this, this, numberString);

      _tvAmount = (TextView) findViewById(R.id.tvAmount);

      String hint = String.format(new Locale(_mbwManager.getLanguage()), "%d-%d", _sellOrderSearchItem.minimumFiat,
            _sellOrderSearchItem.maximumFiat);
      _tvAmount.setHint(hint);

      _btStartTrading = (Button) findViewById(R.id.btStartTrading);
      if (hasMoreThanOneReceivingAddress()) {
         // Show Next instead of Start Trading if the user has more than one
         // receiving address
         _btStartTrading.setText(R.string.lt_next_button);
      }
      _btStartTrading.setOnClickListener(startTradingClickListener);
      ((TextView) findViewById(R.id.tvCurrency)).setText(_sellOrderSearchItem.currency);
   }

   OnClickListener startTradingClickListener = new OnClickListener() {

      @Override
      public void onClick(View v) {
         Address address = _mbwManager.getRecordManager().getWallet(_mbwManager.getWalletMode()).getReceivingAddress();
         CreateInstantBuyOrder request = new CreateInstantBuyOrder(_sellOrderSearchItem.id, getNumber(), address);
         if (hasMoreThanOneReceivingAddress()) {
            // Let the user know which address he is receiving funds to if he
            // has more than one
            CreateInstantBuyOrder2Activity.callMe(CreateInstantBuyOrder1Activity.this, request);
         } else {
            // No need to tell the user which address he receives funds to. He
            // only has one.
            SendRequestActivity.callMe(CreateInstantBuyOrder1Activity.this, request,
                  getString(R.string.lt_place_instant_buy_order_title));
         }
         finish();
      }
   };

   private boolean hasMoreThanOneReceivingAddress() {
      return _mbwManager.getRecordManager().numRecords() > 1;
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putSerializable("amount", getNumber());
   }

   @Override
   protected void onResume() {
      updateUi();
      super.onResume();
   }

   @Override
   protected void onPause() {
      super.onPause();
   }

   @Override
   public void onEntryChanged(String entry) {
      updateUi();
   }

   private void updateUi() {
      Integer number = getNumber();
      if (number == null) {
         // Nothing entered
         _tvAmount.setText("");
         _btStartTrading.setEnabled(false);
      } else if (number < _sellOrderSearchItem.minimumFiat || number > _sellOrderSearchItem.maximumFiat) {
         // Number too small or too large
         _tvAmount.setText(number.toString());
         _tvAmount.setTextColor(getResources().getColor(R.color.red));
         _btStartTrading.setEnabled(false);
      } else {
         // Everything ok
         _tvAmount.setText(number.toString());
         _tvAmount.setTextColor(getResources().getColor(R.color.white));
         _btStartTrading.setEnabled(true);
      }
   }

   private Integer getNumber() {
      try {
         return Integer.parseInt(_numberEntry.getEntry());
      } catch (NumberFormatException e) {
         return null;
      }
   }

}
