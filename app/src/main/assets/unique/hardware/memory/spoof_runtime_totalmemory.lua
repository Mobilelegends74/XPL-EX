--function after(hook, param)
--	local m = "9000000000"
--	log("[Runtime].totalMemory() => " .. m)
--	param:setReturnToLongFromStr(m)
--	return true
--end

function after(hook, param)
	local ret = param:getResultLong()
	if ret == nil then
		return false
	end

    local available = param:getSetting("hardware.memory.available")
    if available == nil then
        return false
    end

    local a = tonumber(available)
    if a == nil then
        return false
    end

    --log("Spoofing totalMemory => [TOTAL]:[" .. available .. "]")

    local fake = param:gigabytesToBytes(a)
    --param:setResultToLong(fake)
    param:setResult(fake)
	return true, ret, param:safe(fake)
end