function after(hook, param)
    local m = param:getArgument(0)
    if m == nil then
        return false
    end

    local total = param:getSetting("hardware.memory.total")
    local avail = param:getSetting("hardware.memory.available")
    if total == nil or avail == nil then
        return false
    end

    local t = tonumber(total)
    local a = tonumber(avail)
    if t == nil or a == nil then
        return false
    end

    local totalBytes = param:gigabytesToBytes(t)
    local availBytes = param:gigabytesToBytes(a)

    m.availMem = availBytes;
    m.totalMem = totalBytes;
    m.threshold = totalBytes / 4;
    m.lowMemory = m.availMem <= m.threshold;
	return true, "N/A", "total=" .. total .. " avail=" .. avail
end