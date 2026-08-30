function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

    local fake = param:getSetting("android.build.date.utc")
    if fake == nil then
        return false
    end

    -- ro.build.date.utc is stored in Unix seconds, while Build.TIME is milliseconds.
    local milliseconds = param:epochSecondsToMillis(fake)
    if milliseconds == nil then
        return false
    end
    param:setResultToLong(milliseconds)
    return true, tostring(ret), milliseconds
end
