function after(hook, param)
    local capacity = param:getSettingInt("battery.capacity.mah", -1)
    if capacity == nil or capacity < 1000 or capacity > 20000 then
        return false
    end

    local original = param:getResult()
    param:setResult(capacity + 0.0)
    return true, param:safe(original), tostring(capacity), "Design capacity (mAh)"
end
