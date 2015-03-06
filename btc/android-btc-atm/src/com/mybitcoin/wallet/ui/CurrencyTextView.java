/*
 * Copyright 2013-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mybitcoin.wallet.ui;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Nonnull;

import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.environment.SettingInfo;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.WalletUtils;
import com.mybitcoin.wallet.R;

/**
 * @author Andreas Schildbach
 */
public final class CurrencyTextView extends TextView
{
	private String prefix = null;
	private ForegroundColorSpan prefixColorSpan = null;
	private BigInteger amount = null;
	private int precision = 0;
	private int shift = 0;
	private boolean alwaysSigned = false;
	private RelativeSizeSpan prefixRelativeSizeSpan = null;
	private RelativeSizeSpan insignificantRelativeSizeSpan = null;
	private Context mContext;

	public CurrencyTextView(final Context context)
	{
		super(context);
		mContext = context;
	}

	public CurrencyTextView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
	}

	public void setPrefix(@Nonnull final String prefix)
	{
		this.prefix = prefix + Constants.CHAR_HAIR_SPACE;
		updateView();
	}

	public void setPrefixColor(final int prefixColor)
	{
		this.prefixColorSpan = new ForegroundColorSpan(prefixColor);
		updateView();
	}

	public void setAmount(@Nonnull final BigInteger amount)
	{
		this.amount = amount;
		updateView();
	}
    public BigInteger getAmount(){
        return this.amount;

    }
	public void setPrecision(final int precision, final int shift)
	{
		this.precision = precision;
		this.shift = shift;
		updateView();
	}

	public void setAlwaysSigned(final boolean alwaysSigned)
	{
		this.alwaysSigned = alwaysSigned;
		updateView();
	}

	public void setStrikeThru(final boolean strikeThru)
	{
		if (strikeThru)
			setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else
			setPaintFlags(getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
	}

	public void setInsignificantRelativeSize(final float insignificantRelativeSize)
	{
		if (insignificantRelativeSize != 1)
		{
			this.prefixRelativeSizeSpan = new RelativeSizeSpan(insignificantRelativeSize);
			this.insignificantRelativeSizeSpan = new RelativeSizeSpan(insignificantRelativeSize);
		}
		else
		{
			this.prefixRelativeSizeSpan = null;
			this.insignificantRelativeSizeSpan = null;
		}
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();

		setPrefixColor(getResources().getColor(R.color.fg_less_significant));
		setInsignificantRelativeSize(0.85f);
		setSingleLine();
	}

	private void updateView()
	{
		final Editable text;

		if (amount != null)
		{
			final String s;
			if (alwaysSigned)
				s = GenericUtils.formatValue(amount, Constants.CURRENCY_PLUS_SIGN, Constants.CURRENCY_MINUS_SIGN, precision, shift);
			else
				s = GenericUtils.formatValue(amount, precision, shift);

			text = new SpannableStringBuilder(s);
			WalletUtils.formatSignificant(text, insignificantRelativeSizeSpan);

			if (prefix != null)
			{
				text.insert(0, prefix);
				if (prefixRelativeSizeSpan != null)
					text.setSpan(prefixRelativeSizeSpan, 0, prefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				if (prefixColorSpan != null)
					text.setSpan(prefixColorSpan, 0, prefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		else
		{
			text = null;
		}

		// AUTHOR: F, DATE: 2014.6.2
		if(text == null)
			mAmountText = null;
		else
			mAmountText = text.toString();
		
		setText(text);
	}
	
	// AUTHOR: F, DATE: 2014.6.2
	private String mAmountText = null;
	public String getAmountText() {
		return mAmountText;
	}
	public void setAmountText(boolean isBuy){
	    if(mAmountText !=null && !mAmountText.equals("")){
	        double num;
	        SettingInfo setInfo = new SettingInfo(mContext);
	        float handlingChargeProportion = setInfo.getHandlingChargeProportion();
	        if(isBuy){
	           num=Float.valueOf(mAmountText)*(1 + handlingChargeProportion);
	        }
	        else{
	            num=Float.valueOf(mAmountText)*(1 - handlingChargeProportion);
	        }
	        BigDecimal numBig = new BigDecimal(num);
	        num=numBig.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
	        mAmountText=num+"";
	        setText(mAmountText);
	    }
	}
	
	public String getAmouText(boolean isBuy){
	    if(isBuy){
	        return this.amount.toString();
	    }
	    else{
	        return this.amount.toString();
	    }
	}
}
