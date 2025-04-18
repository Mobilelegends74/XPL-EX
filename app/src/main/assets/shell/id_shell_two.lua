function after(hook, param)
    --log("Runtime.exec(commands) Hook Invoked")
	--local arg = param:getArgument(0)
	--if arg == nil then
	    --log("Runtime.exec(commands) Input Argument is NULL...")
	--	return false
	--end

	--local command = param:joinStringArray(arg)
	--if command == nil then
	    --log("(commands) joined to (command) is NULL")
	--    return false
	--end

	--log("(commands) joined to (command) value=" .. command)
	--local comRes = param:interceptCommandArray(arg)
	--if comRes == nil then
	    --log("Command Interception returned NULL. Looks clean arg=" .. command)
	--    return false
	--end

    --log("Command was intercepted command=" .. command)
    --return true, command, comRes
    local sh = param:createShellContext(false)
    if sh == nil then

        return false
    end
    local rt = param:isCommandBad(sh)
    if rt == nil then
        return false
    end
    if rt == true then
        return true, param:safe(param:getLogOld()), param:safe(param:getLogNew()), param:safe(param:getLogExtra())
    end
    return false
end