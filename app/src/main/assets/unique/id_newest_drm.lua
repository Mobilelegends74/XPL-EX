function after(hook, param)
	local arg = param:getArgument(0)
	if arg == nil then
	    return false
	end

	if arg == "deviceUniqueId" then
        local res = param:getResult()
        local nme = "unique.drm.id"
        if param:isForceSetting(nme, res) then
            local fake = param:getSetting(nme)
            --Make sure we are using RAW Bytes
            local fakeBys = param:stringToRawBytes(fake)
            param:setResult(fakeBys)
            return true, param:safe(res), param:safe(fakeBys)
        end
	end
    return false
end