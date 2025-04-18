function before(hook, param)
	local ths = param:getThis()
	if ths == nil then
		return false
	end

	local commands = ths:command()
	if commands == nil then
		return false
	end

	local command = param:joinStringList(commands)
	if command == nil then
	    return false
	end

    log("[xlog] ProcessBuilder.start() commands => " .. command)
    return false
end