package eu.faircode.xlua;

/*public class AdapterProp extends RecyclerView.Adapter<AdapterProp.ViewHolder> {
    private static final String TAG = "XLua.ADProp";

    private List<MockProp> props = new ArrayList<>();
    private List<MockProp> props_modified = new ArrayList<>();
    private Object lock = new Object();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public class ViewHolder extends RecyclerView.ViewHolder
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, TextWatcher {

        final View itemView;

        final ImageView ivExpanderProps;
        final TextView tvPropNameLabel;
        final TextView tvDefaultValue;
        final ImageView ivValueResetDefault;
        final CheckBox cbEnableMock;
        final TextInputEditText tiPropMockValue;

        private HashMap<String, Boolean> expanded = new HashMap<>();

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            ivExpanderProps = itemView.findViewById(R.id.ivExpanderProps);
            tvPropNameLabel = itemView.findViewById(R.id.tvPropnameLabel);
            tvDefaultValue = itemView.findViewById(R.id.tvDefaultPropValue);
            ivValueResetDefault = itemView.findViewById(R.id.ivSetDefaultValue);
            cbEnableMock = itemView.findViewById(R.id.cbMockTheProp);
            tiPropMockValue = itemView.findViewById(R.id.tiPropMockValue);
        }

        private void unWire() {
            itemView.setOnClickListener(null);
            cbEnableMock.setOnCheckedChangeListener(null);
            ivValueResetDefault.setOnClickListener(null);
            tiPropMockValue.removeTextChangedListener(this);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            cbEnableMock.setOnCheckedChangeListener(this);
            ivValueResetDefault.setOnClickListener(this);
            tiPropMockValue.addTextChangedListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(final View view) {
            Log.i(TAG, "onClick");
            final MockProp prop = props.get(getAdapterPosition());
            int id = view.getId();
            String name = prop.getName();

            Log.i(TAG, "onClick=" + id + "==" + name);

            switch (view.getId()) {
                case R.id.itemViewProps:
                    if(!expanded.containsKey(name))
                        expanded.put(name, false);

                    expanded.put(name, Boolean.FALSE.equals(expanded.get(name)));
                    updateExpanded();
                    break;
                case R.id.ivSetDefaultValue:
                    if(!prop.getValue().equals(prop.getDefaultValue())) {
                        unWire();
                        tiPropMockValue.setText(prop.getDefaultValue());
                        Log.i(TAG, "Set Text of:" + prop .getName() + " => " + prop.getDefaultValue());
                        prop.setValue(prop.getDefaultValue());
                        wire();
                    }
                    break;
            }
        }

        @Override
        public void onCheckedChanged(final CompoundButton cButton, boolean isChecked) {
            Log.i(TAG, "onCheckedChanged");
            final MockProp prop = props.get(getAdapterPosition());
            final int id = cButton.getId();
            Log.i(TAG, "Item Checked=" + id + "==" + prop.getName());

            switch (id) {
                case R.id.cbMockTheProp:
                    prop.setEnabled(isChecked);
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "prop update result=" +
                                    XMockCallApi.updateProp(
                                            cButton.getContext(),
                                            prop.getName(),
                                            null,
                                            prop.isEnabled()));

                        }
                    });
                    break;
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            MockProp prop = props.get(getAdapterPosition());
            prop.setValue(editable.toString());
            if(!props_modified.contains(prop))
                props_modified.add(prop);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        void updateExpanded() {
            MockProp prop = props.get(getAdapterPosition());
            String name = prop.getName();
            boolean isExpanded = expanded.containsKey(name) && Boolean.TRUE.equals(expanded.get(name));
            ViewUtil.setViewsVisibility(ivExpanderProps, isExpanded, tiPropMockValue, ivValueResetDefault);
        }
    }

    AdapterProp() { setHasStableIds(true); }

    void set(List<MockProp> propValues) {
        props.clear();
        props.addAll(propValues);
        if(DebugUtil.isDebug())
            Log.i(TAG, "Internal Count=" + props.size());

        notifyDataSetChanged();
    }

    public void updateFromModified(final Context context) {
        if(props_modified != null && !props_modified.isEmpty()) {
            Log.i(TAG, "props_modified=" + props_modified.size());
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        final Bundle ret = PutMockPropsCommand.invoke(context, props_modified);
                        props_modified.clear();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void run() {
                                String messageResult = BundleUtil.readResultStatusMessage(ret);
                                Toast.makeText(context, messageResult, Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        }else {
            Toast.makeText(context, R.string.result_settings_update_empty, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public long getItemId(int position) { return props.get(position).hashCode(); }

    @Override
    public int getItemCount() { return props.size(); }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.prop, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.unWire();
        MockProp prop = props.get(position);

        holder.tvPropNameLabel.setText(prop.getName());
        holder.tvDefaultValue.setText(prop.getDefaultValue());
        holder.tiPropMockValue.setText(prop.getValue());
        holder.cbEnableMock.setChecked(prop.isEnabled());

        holder.updateExpanded();
        holder.wire();
    }
}*/
