/***** BEGIN LICENSE BLOCK *****
 * Copyright (c) 2006-2007 Nick Sieger <nick@nicksieger.com>
 * Copyright (c) 2006-2007 Ola Bini <ola.bini@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ***** END LICENSE BLOCK *****/

package jdbc_adapter;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.RubyString;

import org.jruby.runtime.CallbackFactory;
import org.jruby.runtime.builtin.IRubyObject;

import org.jruby.util.ByteList;

public class JdbcMySQLSpec {
    public static void load(Ruby runtime, RubyModule jdbcSpec) {
        RubyModule mysql = jdbcSpec.defineModuleUnder("MySQL");
        CallbackFactory cf = runtime.callbackFactory(JdbcMySQLSpec.class);
        mysql.defineFastMethod("quote_string",cf.getFastSingletonMethod("quote_string",IRubyObject.class));
    }

    private final static ByteList ZERO = new ByteList(new byte[]{'\\','0'});
    private final static ByteList NEWLINE = new ByteList(new byte[]{'\\','n'});
    private final static ByteList CARRIAGE = new ByteList(new byte[]{'\\','r'});
    private final static ByteList ZED = new ByteList(new byte[]{'\\','Z'});
    private final static ByteList DBL = new ByteList(new byte[]{'\\','"'});
    private final static ByteList SINGLE = new ByteList(new byte[]{'\\','\''});
    private final static ByteList ESCAPE = new ByteList(new byte[]{'\\','\\'});

    public static IRubyObject quote_string(IRubyObject recv, IRubyObject string) {
        boolean replacementFound = false;
        ByteList bl = ((RubyString) string).getByteList();
        
        for(int i = bl.begin; i < bl.begin + bl.realSize; i++) {
            ByteList rep = null;
            switch (bl.bytes[i]) {
            case 0: rep = ZERO; break;
            case '\n': rep = NEWLINE; break;
            case '\r': rep = CARRIAGE; break;
            case 26: rep = ZED; break;
            case '"': rep = DBL; break;
            case '\'': rep = SINGLE; break;
            case '\\': rep = ESCAPE; break;
            default: continue;
            }
            
            // On first replacement allocate a different bytelist so we don't manip original 
            if(!replacementFound) {
                i-= bl.begin;
                bl = new ByteList(bl);
                replacementFound = true;
            }

            bl.replace(i, 1, rep);
            i+=1;
        }

        if(!replacementFound) {
            return string;
        }


        return recv.getRuntime().newStringShared(bl);
    }
}