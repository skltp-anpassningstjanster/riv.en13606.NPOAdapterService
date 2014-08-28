/**
 * Copyright (c) 2014 Inera AB, <http://inera.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skl.skltpservices.npoadapter.mapper.util;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.XMLBeanMapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Utility class to create and map common EHR types.
 * @author torbjorncla
 *
 */
public final class EHRUtil {

    private static ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };

    //
    public static String formatTimestamp(Date timestamp) {
        return formatter.get().format(timestamp);
    }

	public static String getElementTextValue(final ELEMENT e) {
		if(e != null && e.getValue() instanceof ST) {
			ST text = (ST) e.getValue();
			return text.getValue();
		}
		return null;
	}
	
	public static String getElementTimeValue(final ELEMENT e) {
		if(e != null && e.getValue() instanceof TS) {
			TS time = (TS) e.getValue();
			return time.getValue();
		}
		return null;
	}
	
	public static ST stType(final String value) {
        if (value == null) {
            return null;
        }
        final ST st = new ST();
        st.setValue(value);
        return st;
    }
	
	public static TS tsType(final String value) {
		final TS ts = new TS();
		ts.setValue(value);
		return ts;
	}
		
	public static INT intType(final int value) {
		final INT _int = new INT();
		_int.setValue(value);
		return _int;
	}
	
	public static String getPartValue(final List<EN> names) {
        final EN item = firstItem(names);
        if (item != null) {
            final ENXP part = firstItem(item.getPart());
            return (part == null) ? null : part.getValue();
        }
        return null;
    }
	
    public static <T> T firstItem(final List<T> list) {
        return (list.size() == 0) ? null : list.get(0);
    }
    
    public static String getCDCode(final CD cd) {
        return (cd == null) ? null : cd.getCode();
    }
    
    public static IDENTIFIEDENTITY lookupDemographicIdentity(final List<IDENTIFIEDENTITY> demographics, final String hsaId) {
        for (final IDENTIFIEDENTITY identifiedentity : demographics) {
            if (hsaId.equals(identifiedentity.getExtractId().getExtension())) {
                return identifiedentity;
            }
        }
        return null;
    }
    
    public static ParameterType createParameter(String name, String value) {
        assert (name != null) && (value != null);
        final ParameterType parameterType = new ParameterType();
        parameterType.setName(stType(name));
        parameterType.setValue(stType(value));
        return parameterType;
    }
    
  	public static ELEMENT findEntryElement(final List<CONTENT> contents, final String type) {
  		for(CONTENT content : contents) {
  			if(content instanceof ENTRY) {
  				ENTRY e = (ENTRY) content;
  				for(ITEM item : e.getItems()) {
  					if(item instanceof ELEMENT) {
  						ELEMENT elm = (ELEMENT) item;
  						if(elm.getMeaning() != null && StringUtils.equals(elm.getMeaning().getCode(), type)) {
  							return elm;
  						}
  					}
  				}
  			}
  		}
  		return null;
  	}
  	
  	public static Boolean boolValue(final ELEMENT elm) {
  		if(elm != null && elm.getValue() instanceof BL) {
  			BL bl = (BL) elm.getValue();
  			return bl.isValue();
  		}
  		return null;
  	}
  	
  	public static String getSystemHSAId(final EHREXTRACT ehrExtract) {
		if(ehrExtract.getEhrSystem() != null) {
			return ehrExtract.getEhrSystem().getExtension();
		}
		return null;
  	}

    //
    static II iiType(final PersonId personId) {
        final II ii = new II();
        if (personId != null) {
            ii.setRoot(personId.getType());
            ii.setExtension(personId.getId());
        }
        return ii;
    }

    //
    public static II iiType(final Object personIdType) {
        return (personIdType == null) ? null : iiType(XMLBeanMapper.getInstance().map(personIdType, PersonId.class));
    }

    //
    static IVLTS IVLTSType(final DatePeriod datePeriod) {
        final IVLTS ivlts = new IVLTS();
        if (datePeriod != null) {
            ivlts.setLow(tsType(datePeriod.getStart()));
            ivlts.setHigh(tsType(datePeriod.getEnd()));
        }
        return ivlts;
    }

    //
    public static IVLTS IVLTSType(final Object datePeriodType) {
        return (datePeriodType == null) ? null : IVLTSType(XMLBeanMapper.getInstance().map(datePeriodType, DatePeriod.class));
    }

    //
    public static <T> T personIdType(final II ii, final Class<T> type) {
        if (ii == null) {
            return null;
        }
        final PersonId personId = new PersonId();
        personId.setId(ii.getExtension());
        personId.setType(ii.getRoot());
        return XMLBeanMapper.getInstance().map(personId, type);
    }

    public static <T> T datePeriod(final IVLTS ivlts, final Class<T> type) {
        if (ivlts == null) {
            return null;
        }
        final DatePeriod datePeriod = new DatePeriod();

        if (ivlts.getHigh() != null) {
            datePeriod.setEnd(ivlts.getHigh().getValue());
        }
        if (ivlts.getLow() != null) {
            datePeriod.setStart(ivlts.getLow().getValue());
        }

        return XMLBeanMapper.getInstance().map(datePeriod, type);
    }

    //
    public static <T> T resultType(final String logId, final List<ResponseDetailType> details, final Class<T> type) {
        if (details.isEmpty()) {
            return null;
        }
        final ResponseDetailType resp = details.get(0);
        final Result result = new Result();
        if (resp.getText() != null) {
            result.setMessage(resp.getText().getValue());
        }
        result.setLogId(logId);
        result.setResultCode(interpret(resp.getTypeCode()));

        return XMLBeanMapper.getInstance().map(result, type);
    }

    //
    public static ResultCode interpret(final ResponseDetailTypeCodes code) {
        switch(code) {
            case E:
            case W:
                return ResultCode.ERROR;
            case I:
                return ResultCode.INFO;
            default:
                return ResultCode.OK;
        }
    }

    //
    @Data
    public static class Result {
        protected ResultCode resultCode;
        protected ErrorCode errorCode;
        protected String logId;
        protected String subCode;
        protected String message;
    }

    //
    public static enum ResultCode {
        OK,
        ERROR,
        INFO;
    }

    //
    public static enum ErrorCode {
        INVALID_REQUEST,
        TRANSFORMATION_ERROR,
        APPLICATION_ERROR,
        TECHNICAL_ERROR;
    }

    //
    @Data
    public static class DatePeriod {
        private String start;
        private String end;
    }

    //
    @Data
    public static class PersonId {
        private String id;
        private String type;
    }
}
