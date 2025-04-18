function before(hook, param)
	local arg = param:getArgument(0)
	if arg == nil then
		return false
	end

	local command = param:joinStringArray(arg)
	if command == nil then
	    return false
	end

	log("[xlog] Runtime.exec(Commands) => " .. command)
	return false
end