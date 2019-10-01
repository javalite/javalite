package org.javalite.activeweb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartForm {

    private List<FormItem> formItems = new ArrayList<>();
    private List<FileItem> fileItems = new ArrayList<>();

    void addFormItem(FormItem formItem){
        formItems.add(formItem);
    }

    void addFileItem(FileItem fileItem){
        fileItems.add(fileItem);
    }

    /**
     * @return all file items
     */
    public List<FileItem> getFileItems() {
        return fileItems;
    }

    /**
     * @return all form items
     */
    public List<FormItem> getFormItems() {
        return formItems;
    }

    /**
     *
     * @param name name of a form input that is not a file
     * @return value of a named input from a form
     */
    public String param(String name){
        for (FormItem formItem : formItems) {
            if(formItem.getFieldName().equals(name)){
                return formItem.getStreamAsString();
            }
        }
        return null;
    }

    /**
     * @return all form items that are  not files.
     */
    public Map<String,String> params(){
        Map<String,String> map = new HashMap<>();
        for (FormItem formItem : formItems) {
            map.put(formItem.getFieldName(), formItem.getStreamAsString());
        }
        return map;
    }
}
