/*
 * Copyright 2015 A.Solyankin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qantium.data.parcers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author A.Solyankin
 */
public class RegexpParcer implements DataParcer {

    private final String regexp;

    public RegexpParcer(String regexp) {
        this.regexp = regexp;
    }

    public String getRegexp() {
        return regexp;
    }
    
    public static <T> T[] parce(String regexp, T data) {
        return new RegexpParcer(regexp).parce(data);
    }

    @Override
    public <T> T[] parce(T data) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(data.toString());
        
        String[] groups;

        if (matcher.find()) {
            
            int groupCount = matcher.groupCount();
            
            groups = new String[groupCount];

            for (int i = 1; i <= groupCount; i++) {
                groups[i - 1] = matcher.group(i);
            }
        } else {
            groups = new String[]{data.toString()};
        }
        return (T[]) groups;
    }
}
