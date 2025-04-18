package eu.faircode.xlua.x.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;

public class SettingsSearchAdapter extends BaseAdapter implements Filterable {
    private final Context context;
    private final List<SettingHolder> originalSettings;
    private List<SettingHolder> filteredSettings;
    private final List<String> checkedSettings;
    private final SettingFilter filter;

    public SettingsSearchAdapter(Context context, List<SettingHolder> settings, List<String> checkedSettings) {
        this.context = context;
        this.originalSettings = settings;
        this.checkedSettings = checkedSettings;
        this.filter = new SettingFilter();

        // Sort settings to show checked items first
        sortSettingsCheckedFirst(settings);
    }

    /**
     * Sorts the settings list to display checked items first, then unchecked items
     * with priority for settings starting with "unique.imei." or "unique.serial"
     */
    private void sortSettingsCheckedFirst(List<SettingHolder> settings) {
        try {
            this.filteredSettings = new ArrayList<>(settings);

            // Sort the list using Collections.sort (API 23 compatible)
            Collections.sort(this.filteredSettings, new Comparator<SettingHolder>() {
                @Override
                public int compare(SettingHolder s1, SettingHolder s2) {
                    // Handle null safety
                    if (s1 == null && s2 == null) return 0;
                    if (s1 == null) return 1; // Null items go to the end
                    if (s2 == null) return -1;

                    try {
                        String name1 = s1.getName();
                        String name2 = s2.getName();

                        boolean isChecked1 = checkedSettings.contains(name1);
                        boolean isChecked2 = checkedSettings.contains(name2);

                        // First priority: checked items
                        if (isChecked1 && !isChecked2) {
                            return -1; // s1 is checked, s2 is not, s1 comes first
                        } else if (!isChecked1 && isChecked2) {
                            return 1;  // s2 is checked, s1 is not, s2 comes first
                        }

                        // If both are checked or both are unchecked, check for priority settings
                        if (!isChecked1 && !isChecked2) {
                            boolean isPriority1 = isPrioritySettingName(name1);
                            boolean isPriority2 = isPrioritySettingName(name2);

                            if (isPriority1 && !isPriority2) {
                                return -1; // s1 is priority, s2 is not, s1 comes first
                            } else if (!isPriority1 && isPriority2) {
                                return 1;  // s2 is priority, s1 is not, s2 comes first
                            }
                        }

                        // If both are in the same category (checked/unchecked, priority/non-priority),
                        // sort alphabetically by display name
                        return s1.getNameNice().compareTo(s2.getNameNice());
                    } catch (Exception e) {
                        // If any comparison fails, maintain original order
                        return 0;
                    }
                }
            });
        } catch (Exception e) {
            // If sorting fails entirely, at least ensure we have a valid list
            if (this.filteredSettings == null || this.filteredSettings.isEmpty()) {
                this.filteredSettings = new ArrayList<>(settings);
            }
        }
    }

    /**
     * Determines if a setting name is a priority setting (unique.imei. or unique.serial)
     */
    private boolean isPrioritySettingName(String name) {
        if (name == null)
            return false;

        for (String s : CoreUiUtils.TOP_PRI_UN_CHECKED) {
            if(name.startsWith(s))
                return true;
        }

        //return name.startsWith("unique.gsm.imei.") || name.startsWith("unique.serial");
        return false;
    }

    @Override
    public int getCount() {
        return filteredSettings.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredSettings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_setting_search, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.tvSettingTitle);
            holder.tvSubText = convertView.findViewById(R.id.tvSettingName);
            holder.cbSelected = convertView.findViewById(R.id.cbSettingSelected);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SettingHolder setting = filteredSettings.get(position);

        CoreUiUtils.setText(holder.tvTitle, setting.getName());

        TryRun.onMain(() -> {
            CoreUiUtils.setText(holder.tvTitle, setting.getName());
            CoreUiUtils.setText(holder.tvSubText, Str.getNonNullOrEmptyString(setting.getContainerName(), setting.getParentName()));
        });

        // Handle checkbox state without triggering listener
        holder.cbSelected.setOnCheckedChangeListener(null);
        boolean isChecked = checkedSettings.contains(setting.getName());
        holder.cbSelected.setChecked(isChecked);

        // Set listener for checkbox
        holder.cbSelected.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            String settingName = setting.getName();
            if (isChecked1) {
                if (!checkedSettings.contains(settingName)) {
                    checkedSettings.add(settingName);
                }
            } else {
                checkedSettings.remove(settingName);
            }

            // Re-sort the list to keep checked items at the top
            sortSettingsCheckedFirst(originalSettings);
            notifyDataSetChanged();
        });

        // Make the entire row clickable to toggle checkbox
        convertView.setOnClickListener(v -> {
            holder.cbSelected.toggle();
            String settingName = setting.getName();
            if (holder.cbSelected.isChecked()) {
                if (!checkedSettings.contains(settingName)) {
                    checkedSettings.add(settingName);
                }
            } else {
                checkedSettings.remove(settingName);
            }

            // Re-sort the list to keep checked items at the top
            sortSettingsCheckedFirst(originalSettings);
            notifyDataSetChanged();
        });

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public List<String> getCheckedSettings() {
        return checkedSettings;
    }

    private static class ViewHolder {
        TextView tvTitle;
        TextView tvSubText;
        CheckBox cbSelected;
    }

    private class SettingFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented, return whole list
                results.values = originalSettings;
                results.count = originalSettings.size();
            } else {
                // Perform filtering operation
                List<SettingHolder> filteredList = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (SettingHolder setting : originalSettings) {
                    if (    setting.getNameNice().toLowerCase().contains(filterPattern) ||
                            setting.getName().toLowerCase().contains(filterPattern) ||
                            Str.contains(setting.getDescription(), filterPattern, true) ||
                    Str.contains(setting.getContainerName(), filterPattern, true)) {
                        filteredList.add(setting);
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            try {
                List<SettingHolder> filtered = (List<SettingHolder>) results.values;

                // Re-sort the filtered results to maintain priority order
                Collections.sort(filtered, new Comparator<SettingHolder>() {
                    @Override
                    public int compare(SettingHolder s1, SettingHolder s2) {
                        // Handle null safety
                        if (s1 == null && s2 == null) return 0;
                        if (s1 == null) return 1; // Null items go to the end
                        if (s2 == null) return -1;

                        try {
                            String name1 = s1.getName();
                            String name2 = s2.getName();

                            boolean isChecked1 = checkedSettings.contains(name1);
                            boolean isChecked2 = checkedSettings.contains(name2);

                            // First priority: checked items
                            if (isChecked1 && !isChecked2) {
                                return -1; // s1 is checked, s2 is not, s1 comes first
                            } else if (!isChecked1 && isChecked2) {
                                return 1;  // s2 is checked, s1 is not, s2 comes first
                            }

                            // If both are checked or both are unchecked, check for priority settings
                            if (!isChecked1 && !isChecked2) {
                                boolean isPriority1 = isPrioritySettingName(name1);
                                boolean isPriority2 = isPrioritySettingName(name2);

                                if (isPriority1 && !isPriority2) {
                                    return -1; // s1 is priority, s2 is not, s1 comes first
                                } else if (!isPriority1 && isPriority2) {
                                    return 1;  // s2 is priority, s1 is not, s2 comes first
                                }
                            }

                            // If both are in the same category, sort alphabetically by display name
                            return s1.getNameNice().compareTo(s2.getNameNice());
                        } catch (Exception e) {
                            // If any comparison fails, maintain original order
                            return 0;
                        }
                    }
                });

                filteredSettings = filtered;
                notifyDataSetChanged();
            } catch (Exception e) {
                // If anything goes wrong, at least ensure we have a valid list
                if (filteredSettings == null || filteredSettings.isEmpty()) {
                    filteredSettings = new ArrayList<>(originalSettings);
                }
                notifyDataSetChanged();
            }
        }
    }
}