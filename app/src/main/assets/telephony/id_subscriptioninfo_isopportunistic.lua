function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

    local setting = param:getSetting("cell.data.is.opportunistic.1", "false")
    if setting == nil then
        return false
    end

    local fake = false
    if setting == "true" then
        fake = true
    end

	param:setResult(fake)
	return true, tostring(ret), tostring(fake)
end