package chenmc.sms.utils.storage;

import android.provider.BaseColumns;

/**
 * Created by 明明 on 2017/7/28.
 */

public class CodeMatchRuleTable {
    public static final String TABLE_NAME = "SmsMatchRules";
    
    public class Columns implements BaseColumns {
        public static final String SMS = "sms";
        public static final String CODE = "code";
        public static final String RULE = "rule";
    }
}
