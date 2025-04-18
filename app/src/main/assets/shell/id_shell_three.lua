function after(hook, param)
    --log("ProcessBuilder.start() hook Invoked")
	--local ths = param:getThis()
	--if ths == nil then
	    --log("ProcessBuilder.start() (this) context is NULL")
	--	return false
	--end

	--local commands = ths:command()
	--if commands == nil then
	    --log("(commands) is NULL")
	--	return false
	--end

	--local command = param:joinStringList(commands)
	--if command == nil then
	    --log("(commands) joined into (command) is NULL")
	--    return false
	--end

    --log("ProcessBuilder.start() (commands) joined into (command) =" .. command)
	--local comRes = param:interceptCommandList(commands)
	--if comRes == nil then
	    --log("Result from (" .. command .. ") returned NULL. Command looks clean!")
	--    return false
	--end

	--log("Command (" .. command .. ") was intercepted !")
    --return true, command, comRes
    local sh = param:createShellContext(true)
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